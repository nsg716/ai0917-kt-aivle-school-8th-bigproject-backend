"""
웹소설 장르 및 IP 트렌드 종합 분석 + PDF 보고서 자동 생성 (통합 버전)

[실행 흐름]
1단계: 데이터 수집 및 분석
  - 12개 장르 Google Trends 분석 (복수 키워드 평균)
  - IP 확장 트렌드 분석 (웹툰화, 드라마화, 영화화, 게임화)
  - 1개월, 3개월, 12개월 추이 분석
  - 증감률 계산
  - 그래프 및 CSV 생성

2단계: PDF 보고서 생성
  - 수집된 데이터 기반 전문 보고서 자동 생성
  - 표, 그래프, 통계 포함
  - 실행 가능한 전략 제시

작성자: Claude
버전: 2.0 (통합)
"""

import subprocess
import sys
import os
from time import sleep
from datetime import datetime

# UTF-8 인코딩 설정 (Windows cp949 문제 해결)
if sys.platform == 'win32':
    import io
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8', errors='replace')

############################################
# 필수 라이브러리 설치
############################################

def install_requirements():
    """필수 라이브러리 일괄 설치"""
    print("=" * 80)
    print("필수 라이브러리 확인 및 설치")
    print("=" * 80)
    
    packages = [
        'pytrends',
        'pandas', 
        'matplotlib',
        'koreanize-matplotlib',
        'reportlab',
        'numpy'
    ]
    
    for package in packages:
        try:
            __import__(package.replace('-', '_'))
        except ImportError:
            print(f"-> {package} 설치 중...")
            subprocess.check_call([
                sys.executable, "-m", "pip", "install", 
                package, "--break-system-packages"
            ], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            print(f"  [OK] {package} 설치 완료")
    
    print("\n[OK] 모든 라이브러리 준비 완료\n")

install_requirements()

# 라이브러리 임포트
from pytrends.request import TrendReq
import pandas as pd
import matplotlib.pyplot as plt
import matplotlib
import koreanize_matplotlib
from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.units import inch
from reportlab.platypus import (SimpleDocTemplate, Table, TableStyle, Paragraph, 
                                 Spacer, PageBreak, Image)
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_JUSTIFY
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
import numpy as np

############################################
# 전역 설정
############################################

# 한글 폰트 설정 (PDF용)
def setup_pdf_korean_font():
    """PDF용 한글 폰트 등록"""
    try:
        import urllib.request
        import tempfile
        
        font_url = "https://github.com/google/fonts/raw/main/ofl/nanumgothic/NanumGothic-Regular.ttf"
        temp_dir = tempfile.gettempdir()
        font_path = os.path.join(temp_dir, "NanumGothic.ttf")
        
        if not os.path.exists(font_path):
            print("-> PDF용 한글 폰트 다운로드 중...")
            urllib.request.urlretrieve(font_url, font_path)
        
        pdfmetrics.registerFont(TTFont('NanumGothic', font_path))
        return 'NanumGothic'
    except Exception as e:
        print(f"  [WARNING] 폰트 등록 실패: {e}, 기본 폰트 사용")
        return 'Helvetica'

KOREAN_FONT = setup_pdf_korean_font()

# pytrends 초기화
pytrends = TrendReq(hl='ko', tz=540)

# 분석 대상 키워드
GENRE_KEYWORDS = {
    "로맨스": ["로맨스 웹소설", "로맨스소설", "로맨스"],
    "로판": ["로판 웹소설", "로맨스판타지", "로판"],
    "판타지": ["판타지 웹소설", "판타지소설", "판타지"],
    "현판": ["현대판타지", "현판 웹소설", "현판"],
    "무협": ["무협 웹소설", "무협소설", "무협"],
    "미스터리": ["미스터리 웹소설", "추리소설", "미스터리"],
    "라이트노벨": ["라이트노벨", "라노벨", "라이트노벨 웹소설"],
    "BL": ["BL 웹소설", "BL소설", "보이즈러브"],
    "드라마": ["드라마 웹소설", "드라마소설"],
    "액션": ["액션 웹소설", "액션소설", "액션"],
    "패러디": ["패러디 웹소설", "패러디소설"],
    "문학": ["순문학", "문학소설", "문학"]
}

IP_EXPANSION_KEYWORDS = {
    "웹툰화": ["웹소설 웹툰화", "소설 웹툰", "웹툰 원작"],
    "드라마화": ["웹소설 드라마", "소설 드라마", "드라마 원작"],
    "영화화": ["웹소설 영화", "소설 영화", "영화 원작"],
    "게임화": ["웹소설 게임", "소설 게임"]
}

TIMEFRAMES = {
    "1개월": "today 1-m",
    "3개월": "today 3-m",
    "12개월": "today 12-m"
}

############################################
# PART 1: 데이터 수집 및 분석
############################################

class TrendAnalyzer:
    """Google Trends 데이터 수집 및 분석 클래스"""
    
    def __init__(self):
        self.all_results = {}
        self.failed_items = {
            '1개월': {'genre': [], 'ip': []},
            '3개월': {'genre': [], 'ip': []},
            '12개월': {'genre': [], 'ip': []}
        }
    
    def get_single_keyword_trend(self, keywords, timeframe, max_retries=5):
        """단일 키워드 그룹의 평균 검색량 수집 (429 에러 방지 강화)"""
        for attempt in range(max_retries):
            try:
                pytrends.build_payload(kw_list=keywords, timeframe=timeframe)
                data = pytrends.interest_over_time()
                
                if data.empty:
                    return None
                
                if 'isPartial' in data.columns:
                    data = data.drop('isPartial', axis=1)
                
                avg_trend = data[keywords].mean(axis=1)
                return avg_trend
                
            except Exception as e:
                error_msg = str(e)
                
                # 429 에러인 경우 더 긴 대기
                if '429' in error_msg:
                    wait_time = 15 + (attempt * 10)  # 15초, 25초, 35초...
                    print(f"      [WARNING] API 제한 감지 - {wait_time}초 대기 후 재시도 {attempt + 1}/{max_retries}...")
                    sleep(wait_time)
                else:
                    wait_time = 5 + (attempt * 3)
                    print(f"      재시도 {attempt + 1}/{max_retries}... ({e})")
                    sleep(wait_time)
                
                if attempt == max_retries - 1:
                    print(f"      [FAIL] 최종 실패: {e}")
                    return None
        
        return None
    
    def get_all_trends(self, keywords_dict, timeframe, period_name, data_type='genre'):
        """모든 키워드의 검색량 수집 (429 에러 방지 강화)"""
        result_data = {}
        total = len(keywords_dict)
        
        for idx, (name, keywords) in enumerate(keywords_dict.items(), 1):
            print(f"    [{idx}/{total}] 분석 중: {name} ({', '.join(keywords)})")
            
            avg_trend = self.get_single_keyword_trend(keywords, timeframe)
            
            if avg_trend is not None:
                result_data[name] = avg_trend
                print(f"        [OK] 성공")
            else:
                print(f"        [WARNING] 데이터 없음 - 재시도 목록에 추가")
                # 실패한 항목 기록
                self.failed_items[period_name][data_type].append((name, keywords))
            
            # 429 에러 방지를 위한 충분한 대기 (마지막 항목 제외)
            if idx < total:
                wait_time = 10
                print(f"        대기 중... ({wait_time}초)")
                sleep(wait_time)
        
        if result_data:
            return pd.DataFrame(result_data)
        else:
            return pd.DataFrame()
    
    def calculate_statistics(self, data):
        """통계 계산"""
        stats = []
        
        for col in data.columns:
            stats.append({
                '항목': col,
                '평균': round(data[col].mean(), 2),
                '최대': int(data[col].max()),
                '최소': int(data[col].min()),
                '표준편차': round(data[col].std(), 2)
            })
        
        return pd.DataFrame(stats).sort_values('평균', ascending=False)
    
    def calculate_changes(self, item_type='genre'):
        """이전 기간 대비 증감량 계산"""
        periods = list(TIMEFRAMES.keys())
        changes_data = []
        
        for i in range(len(periods)):
            current_period = periods[i]
            
            if current_period not in self.all_results:
                continue
            
            current_stats = self.all_results[current_period][f'{item_type}_stats']
            
            for _, row in current_stats.iterrows():
                item = row['항목']
                current_avg = row['평균']
                
                change = 0
                change_rate = 0
                
                if i > 0:
                    prev_period = periods[i-1]
                    if prev_period in self.all_results:
                        prev_stats = self.all_results[prev_period][f'{item_type}_stats']
                        prev_row = prev_stats[prev_stats['항목'] == item]
                        
                        if not prev_row.empty:
                            prev_avg = prev_row.iloc[0]['평균']
                            change = current_avg - prev_avg
                            change_rate = ((current_avg - prev_avg) / prev_avg * 100) if prev_avg > 0 else 0
                
                changes_data.append({
                    '기간': current_period,
                    '항목': item,
                    '평균': current_avg,
                    '최대': row['최대'],
                    '최소': row['최소'],
                    '표준편차': row['표준편차'],
                    '증감량': round(change, 2),
                    '증감률(%)': round(change_rate, 2)
                })
        
        return pd.DataFrame(changes_data)
    
    def plot_trends(self, data, period, title_prefix="장르"):
        """검색량 추이 그래프"""
        if data.empty:
            return
        
        plt.figure(figsize=(15, 8))
        
        for col in data.columns:
            plt.plot(data.index, data[col], marker='o', label=col, linewidth=2, markersize=4)
        
        plt.title(f'{title_prefix} 트렌드 분석 - {period}', fontsize=16, fontweight='bold', pad=20)
        plt.xlabel('날짜', fontsize=12)
        plt.ylabel('검색 관심도 (0-100)', fontsize=12)
        plt.legend(loc='best', fontsize=9, ncol=2)
        plt.grid(True, alpha=0.3)
        plt.xticks(rotation=45)
        plt.tight_layout()
        
        filename = f"{title_prefix}_trends_{period.replace('개월', 'm')}.png"
        plt.savefig(filename, dpi=300, bbox_inches='tight')
        print(f"    [OK] 그래프 저장: {filename}")
        plt.close()
    
    def plot_ranking(self, stats, period, title_prefix="장르"):
        """순위 막대 그래프"""
        if stats.empty:
            return
        
        plt.figure(figsize=(12, 7))
        
        colors_list = plt.cm.viridis(range(len(stats)))
        bars = plt.barh(stats['항목'], stats['평균'], color=colors_list)
        
        for i, (idx, row) in enumerate(stats.iterrows()):
            plt.text(row['평균'] + 0.5, i, f"{row['평균']:.1f}", 
                    va='center', fontsize=10)
        
        plt.title(f'{title_prefix} 평균 검색량 순위 - {period}', fontsize=16, fontweight='bold', pad=20)
        plt.xlabel('평균 검색 관심도', fontsize=12)
        plt.ylabel('항목', fontsize=12)
        plt.grid(True, alpha=0.3, axis='x')
        plt.tight_layout()
        
        filename = f"{title_prefix}_ranking_{period.replace('개월', 'm')}.png"
        plt.savefig(filename, dpi=300, bbox_inches='tight')
        print(f"    [OK] 그래프 저장: {filename}")
        plt.close()
    
    def plot_comparison(self, item_type='genre'):
        """기간별 비교 그래프"""
        periods = list(TIMEFRAMES.keys())
        valid_periods = [p for p in periods if p in self.all_results and not self.all_results[p][f'{item_type}_stats'].empty]
        
        if not valid_periods:
            print(f"    [WARNING] {item_type} 비교 그래프: 유효한 데이터 없음")
            return
        
        first_period_stats = self.all_results[valid_periods[0]][f'{item_type}_stats']
        top_items = first_period_stats.head(8)['항목'].tolist()
        
        comparison_data = {item: [] for item in top_items}
        
        for period in valid_periods:
            stats = self.all_results[period][f'{item_type}_stats']
            for item in top_items:
                row = stats[stats['항목'] == item]
                if not row.empty:
                    comparison_data[item].append(row.iloc[0]['평균'])
                else:
                    comparison_data[item].append(0)
        
        plt.figure(figsize=(14, 8))
        
        x = range(len(valid_periods))
        width = 0.08
        
        for i, item in enumerate(top_items):
            offset = (i - len(top_items)/2) * width
            plt.bar([xi + offset for xi in x], comparison_data[item], 
                   width, label=item)
        
        plt.xlabel('분석 기간', fontsize=12)
        plt.ylabel('평균 검색 관심도', fontsize=12)
        plt.title(f'기간별 {item_type} 트렌드 비교 (Top 8)', fontsize=16, fontweight='bold', pad=20)
        plt.xticks(x, valid_periods)
        plt.legend(loc='best', fontsize=9, ncol=2)
        plt.grid(True, alpha=0.3, axis='y')
        plt.tight_layout()
        
        filename = f"{item_type}_period_comparison.png"
        plt.savefig(filename, dpi=300, bbox_inches='tight')
        print(f"    [OK] 기간별 비교 그래프 저장: {filename}")
        plt.close()
    
    def run_analysis(self):
        """전체 분석 실행"""
        print("\n" + "=" * 80)
        print("PART 1: 데이터 수집 및 분석 시작")
        print("=" * 80)
        print(f"\n분석 장르: {len(GENRE_KEYWORDS)}개")
        print(f"IP 확장: {len(IP_EXPANSION_KEYWORDS)}개")
        print(f"분석 기간: {', '.join(TIMEFRAMES.keys())}\n")
        
        for period_name, timeframe in TIMEFRAMES.items():
            print(f"\n{'=' * 80}")
            print(f"[{period_name} 분석 시작]")
            print(f"{'=' * 80}")
            
            # 장르 트렌드 수집
            print(f"\n  [1단계] 장르별 트렌드 수집")
            genre_data = self.get_all_trends(GENRE_KEYWORDS, timeframe, period_name, 'genre')
            
            if genre_data.empty:
                print(f"    [WARNING] {period_name} 장르 데이터 수집 실패")
                continue
            
            genre_stats = self.calculate_statistics(genre_data)
            
            print(f"\n  [{period_name} 장르 통계 TOP 5]")
            print("  " + "-" * 76)
            for idx, row in genre_stats.head(5).iterrows():
                print(f"    {idx+1}위: {row['항목']:12s} | 평균: {row['평균']:6.2f} | "
                      f"최대: {row['최대']:3d} | 최소: {row['최소']:3d}")
            
            # CSV 저장
            genre_data.to_csv(f"genre_data_{period_name}.csv", encoding='utf-8-sig')
            genre_stats.to_csv(f"genre_stats_{period_name}.csv", index=False, encoding='utf-8-sig')
            print(f"\n    [OK] CSV 저장 완료")
            
            # 그래프 생성 (순위 막대 그래프만)
            print(f"\n  [2단계] 장르 그래프 생성")
            self.plot_ranking(genre_stats, period_name, "장르")
            
            # 장르와 IP 확장 사이 대기 (429 에러 방지)
            print(f"\n  IP 확장 분석 준비 중... (10초 대기)")
            sleep(10)
            
            # IP 확장 트렌드
            print(f"\n  [3단계] IP 확장 트렌드 수집")
            ip_data = self.get_all_trends(IP_EXPANSION_KEYWORDS, timeframe, period_name, 'ip')
            
            ip_stats = pd.DataFrame()
            if not ip_data.empty:
                ip_stats = self.calculate_statistics(ip_data)
                
                print(f"\n  [{period_name} IP 확장 통계]")
                print("  " + "-" * 76)
                for idx, row in ip_stats.iterrows():
                    print(f"    {idx+1}위: {row['항목']:10s} | 평균: {row['평균']:6.2f}")
                
                ip_data.to_csv(f"ip_expansion_data_{period_name}.csv", encoding='utf-8-sig')
                ip_stats.to_csv(f"ip_expansion_stats_{period_name}.csv", index=False, encoding='utf-8-sig')
                
                print(f"\n  [4단계] IP 확장 그래프 생성")
                self.plot_ranking(ip_stats, period_name, "IP확장")
            
            self.all_results[period_name] = {
                'genre_data': genre_data,
                'genre_stats': genre_stats,
                'ip_data': ip_data,
                'ip_stats': ip_stats
            }
            
            print(f"\n  [OK] {period_name} 분석 완료")
            
            # 다음 기간 분석 전 충분한 대기 (429 에러 방지)
            if period_name != list(TIMEFRAMES.keys())[-1]:
                print(f"  다음 기간 분석 준비 중... (10초 대기)")
                sleep(10)
        
        # 증감량 분석
        if len(self.all_results) > 0:
            print(f"\n{'=' * 80}")
            print("[증감량 분석]")
            print(f"{'=' * 80}")
            
            genre_changes = self.calculate_changes('genre')
            genre_changes.to_csv('genre_changes_analysis.csv', index=False, encoding='utf-8-sig')
            print(f"  [OK] 장르 증감량 분석 완료")
            
            if any(not self.all_results[p]['ip_stats'].empty for p in self.all_results):
                ip_changes = self.calculate_changes('ip')
                ip_changes.to_csv('ip_expansion_changes_analysis.csv', index=False, encoding='utf-8-sig')
                print(f"  [OK] IP 확장 증감량 분석 완료")
        
        # 비교 그래프
        if len(self.all_results) > 1:
            print(f"\n{'=' * 80}")
            print("[기간별 비교 그래프 생성]")
            print(f"{'=' * 80}")
            
            self.plot_comparison('genre')
            if any(not self.all_results[p]['ip_stats'].empty for p in self.all_results):
                self.plot_comparison('ip')
        
        # 요약 파일
        print(f"\n{'=' * 80}")
        print("[전체 요약 파일 생성]")
        print(f"{'=' * 80}")
        
        genre_summary = []
        for period_name in TIMEFRAMES.keys():
            if period_name in self.all_results:
                stats = self.all_results[period_name]['genre_stats']
                for _, row in stats.iterrows():
                    genre_summary.append({
                        '기간': period_name,
                        '장르': row['항목'],
                        '평균': row['평균'],
                        '최대': row['최대'],
                        '최소': row['최소'],
                        '표준편차': row['표준편차']
                    })
        
        if genre_summary:
            pd.DataFrame(genre_summary).to_csv('genre_summary_all.csv', index=False, encoding='utf-8-sig')
            print(f"  [OK] 장르 전체 요약 완료")
        
        ip_summary = []
        for period_name in TIMEFRAMES.keys():
            if period_name in self.all_results and not self.all_results[period_name]['ip_stats'].empty:
                stats = self.all_results[period_name]['ip_stats']
                for _, row in stats.iterrows():
                    ip_summary.append({
                        '기간': period_name,
                        'IP확장': row['항목'],
                        '평균': row['평균'],
                        '최대': row['최대'],
                        '최소': row['최소'],
                        '표준편차': row['표준편차']
                    })
        
        if ip_summary:
            pd.DataFrame(ip_summary).to_csv('ip_expansion_summary_all.csv', index=False, encoding='utf-8-sig')
            print(f"  [OK] IP 확장 전체 요약 완료")
        
        print(f"\n{'=' * 80}")
        print("[OK] PART 1 완료: 모든 데이터 수집 및 분석 완료")
        print(f"{'=' * 80}\n")
        
        return True
    
    def retry_failed_items(self):
        """실패한 항목 재시도"""
        try:
            print("\n" + "=" * 80)
            print("실패한 항목 재시도")
            print("=" * 80)
            
            total_failed = sum(
                len(self.failed_items[period]['genre']) + len(self.failed_items[period]['ip'])
                for period in TIMEFRAMES.keys()
            )
            
            if total_failed == 0:
                print("\n  [OK] 실패한 항목 없음 - 재시도 불필요\n")
                return True
            
            print(f"\n  총 {total_failed}개 항목 재시도 시작...")
            print("  [WAIT] 재시도 전 30초 대기 (API 제한 해제 대기)\n")
            sleep(30)
            
            retry_success = True
            
            for period_name, timeframe in TIMEFRAMES.items():
                if period_name not in self.all_results:
                    continue
                
                # 장르 재시도
                failed_genres = self.failed_items[period_name]['genre']
                if failed_genres:
                    print(f"\n[{period_name}] 장르 재시도 ({len(failed_genres)}개)")
                    
                    for name, keywords in failed_genres:
                        print(f"  재시도: {name} ({', '.join(keywords)})")
                        avg_trend = self.get_single_keyword_trend(keywords, timeframe)
                        
                        if avg_trend is not None:
                            # 기존 데이터에 추가
                            self.all_results[period_name]['genre_data'][name] = avg_trend
                            print(f"    [OK] 성공")
                            
                            # 통계 재계산
                            self.all_results[period_name]['genre_stats'] = self.calculate_statistics(
                                self.all_results[period_name]['genre_data']
                            )
                            
                            # CSV 재저장
                            self.all_results[period_name]['genre_data'].to_csv(
                                f"genre_data_{period_name}.csv", encoding='utf-8-sig'
                            )
                            self.all_results[period_name]['genre_stats'].to_csv(
                                f"genre_stats_{period_name}.csv", index=False, encoding='utf-8-sig'
                            )
                            
                            # 그래프 재생성
                            self.plot_ranking(
                                self.all_results[period_name]['genre_stats'], 
                                period_name, 
                                "장르"
                            )
                        else:
                            print(f"    [FAIL] 재시도 실패")
                            retry_success = False
                        
                        sleep(10)
                
                # IP 확장 재시도
                failed_ips = self.failed_items[period_name]['ip']
                if failed_ips:
                    print(f"\n[{period_name}] IP 확장 재시도 ({len(failed_ips)}개)")
                    
                    for name, keywords in failed_ips:
                        print(f"  재시도: {name} ({', '.join(keywords)})")
                        avg_trend = self.get_single_keyword_trend(keywords, timeframe)
                        
                        if avg_trend is not None:
                            # 기존 데이터에 추가
                            if self.all_results[period_name]['ip_data'].empty:
                                self.all_results[period_name]['ip_data'] = pd.DataFrame({name: avg_trend})
                            else:
                                self.all_results[period_name]['ip_data'][name] = avg_trend
                            
                            print(f"    [OK] 성공")
                            
                            # 통계 재계산
                            self.all_results[period_name]['ip_stats'] = self.calculate_statistics(
                                self.all_results[period_name]['ip_data']
                            )
                            
                            # CSV 재저장
                            self.all_results[period_name]['ip_data'].to_csv(
                                f"ip_expansion_data_{period_name}.csv", encoding='utf-8-sig'
                            )
                            self.all_results[period_name]['ip_stats'].to_csv(
                                f"ip_expansion_stats_{period_name}.csv", index=False, encoding='utf-8-sig'
                            )
                            
                            # 그래프 재생성
                            self.plot_ranking(
                                self.all_results[period_name]['ip_stats'], 
                                period_name, 
                                "IP확장"
                            )
                        else:
                            print(f"    [FAIL] 재시도 실패")
                            retry_success = False
                        
                        sleep(10)
            
            # 재시도 후 증감량 및 비교 그래프 재생성
            if retry_success:
                print(f"\n  재생성: 증감량 분석 및 비교 그래프")
                
                genre_changes = self.calculate_changes('genre')
                genre_changes.to_csv('genre_changes_analysis.csv', index=False, encoding='utf-8-sig')
                
                if any(not self.all_results[p]['ip_stats'].empty for p in self.all_results):
                    ip_changes = self.calculate_changes('ip')
                    ip_changes.to_csv('ip_expansion_changes_analysis.csv', index=False, encoding='utf-8-sig')
                
                if len(self.all_results) > 1:
                    self.plot_comparison('genre')
                    if any(not self.all_results[p]['ip_stats'].empty for p in self.all_results):
                        self.plot_comparison('ip')
            
            print(f"\n{'=' * 80}")
            if retry_success:
                print("[OK] 재시도 완료: 모든 항목 수집 성공")
            else:
                print("[WARNING] 재시도 완료: 일부 항목 여전히 실패")
            print(f"{'=' * 80}\n")
            
            return retry_success
            
        except Exception as e:
            print(f"\n[ERROR] 재시도 중 오류 발생: {str(e)}")
            print("[WARNING] 재시도를 건너뛰고 PDF 생성을 계속 진행합니다.\n")
            return False

############################################
# PART 2: PDF 보고서 생성
############################################

class TrendDataLoader:
    """수집된 데이터 로더"""
    
    def __init__(self):
        self.periods = ['1개월', '3개월', '12개월']
        self.genre_data = {}
        self.ip_data = {}
        self.changes_data = {}
        
    def load_all_data(self):
        """모든 데이터 로드"""
        print("\n[데이터 로딩] 분석 결과 파일 읽기 중...")
        
        for period in self.periods:
            self.genre_data[period] = {
                'stats': self._load_csv(f"genre_stats_{period}.csv"),
                'data': self._load_csv(f"genre_data_{period}.csv")
            }
            self.ip_data[period] = {
                'stats': self._load_csv(f"ip_expansion_stats_{period}.csv"),
                'data': self._load_csv(f"ip_expansion_data_{period}.csv")
            }
        
        self.changes_data['genre'] = self._load_csv('genre_changes_analysis.csv')
        self.changes_data['ip'] = self._load_csv('ip_expansion_changes_analysis.csv')
        
        print("  [OK] 데이터 로딩 완료")
        
    def _load_csv(self, filename):
        """CSV 파일 안전 로드"""
        if os.path.exists(filename):
            try:
                return pd.read_csv(filename, encoding='utf-8-sig')
            except Exception as e:
                return pd.DataFrame()
        return pd.DataFrame()
    
    def get_period_stats(self, period, data_type='genre'):
        """특정 기간의 통계 데이터 반환"""
        if data_type == 'genre':
            return self.genre_data.get(period, {}).get('stats', pd.DataFrame())
        else:
            return self.ip_data.get(period, {}).get('stats', pd.DataFrame())
    
    def get_changes(self, period, data_type='genre'):
        """특정 기간의 증감량 데이터 반환"""
        df = self.changes_data.get(data_type, pd.DataFrame())
        if not df.empty:
            return df[df['기간'] == period]
        return pd.DataFrame()


class InsightExtractor:
    """인사이트 추출기"""
    
    @staticmethod
    def get_top_genres(stats_df, n=3):
        if stats_df.empty:
            return []
        return stats_df.head(n)['항목'].tolist()
    
    @staticmethod
    def get_top_growth(changes_df, n=3):
        if changes_df.empty:
            return pd.DataFrame()
        return changes_df.nlargest(n, '증감률(%)')
    
    @staticmethod
    def get_top_decline(changes_df, n=3):
        if changes_df.empty:
            return pd.DataFrame()
        decline = changes_df[changes_df['증감률(%)'] < 0]
        if decline.empty:
            return pd.DataFrame()
        return decline.nsmallest(n, '증감률(%)')
    
    @staticmethod
    def generate_market_summary(all_stats):
        summary = {
            'total_genres': 0,
            'avg_interest': 0,
            'high_volatility': []
        }
        
        if not all_stats.empty:
            summary['total_genres'] = len(all_stats)
            summary['avg_interest'] = all_stats['평균'].mean()
            
            if '표준편차' in all_stats.columns:
                high_vol = all_stats.nlargest(3, '표준편차')
                summary['high_volatility'] = high_vol['항목'].tolist()
        
        return summary


class PDFReportGenerator:
    """PDF 보고서 생성기"""
    
    def __init__(self, data_loader, extractor):
        self.data_loader = data_loader
        self.extractor = extractor
        self.styles = self.create_styles()
    
    def create_styles(self):
        """PDF 스타일 생성"""
        styles = getSampleStyleSheet()
        
        styles.add(ParagraphStyle(
            name='CustomTitle',
            parent=styles['Title'],
            fontName=KOREAN_FONT,
            fontSize=28,
            textColor=colors.HexColor('#1a237e'),
            spaceAfter=30,
            alignment=TA_CENTER,
            leading=36
        ))
        
        styles.add(ParagraphStyle(
            name='CustomSubtitle',
            parent=styles['Normal'],
            fontName=KOREAN_FONT,
            fontSize=16,
            textColor=colors.HexColor('#3f51b5'),
            spaceAfter=12,
            alignment=TA_CENTER,
            leading=20
        ))
        
        styles.add(ParagraphStyle(
            name='SectionTitle',
            parent=styles['Heading1'],
            fontName=KOREAN_FONT,
            fontSize=20,
            textColor=colors.HexColor('#1976d2'),
            spaceAfter=16,
            spaceBefore=20,
            leading=24
        ))
        
        styles.add(ParagraphStyle(
            name='SubsectionTitle',
            parent=styles['Heading2'],
            fontName=KOREAN_FONT,
            fontSize=16,
            textColor=colors.HexColor('#0288d1'),
            spaceAfter=12,
            spaceBefore=16,
            leading=20
        ))
        
        styles.add(ParagraphStyle(
            name='KoreanBody',
            parent=styles['Normal'],
            fontName=KOREAN_FONT,
            fontSize=11,
            leading=16,
            alignment=TA_JUSTIFY,
            spaceAfter=10
        ))
        
        styles.add(ParagraphStyle(
            name='InsightBox',
            parent=styles['Normal'],
            fontName=KOREAN_FONT,
            fontSize=11,
            leading=16,
            textColor=colors.HexColor('#d84315'),
            leftIndent=20,
            rightIndent=20,
            spaceAfter=12,
            spaceBefore=12,
            backColor=colors.HexColor('#fff3e0'),
            borderWidth=1,
            borderColor=colors.HexColor('#ff9800'),
            borderPadding=10
        ))
        
        return styles
    
    def create_cover_page(self):
        """표지 페이지"""
        story = []
        
        story.append(Spacer(1, 2*inch))
        
        today = datetime.now()
        title_text = f"[{today.year}년 {today.month}월] 웹소설 장르 및 IP 확장 트렌드 분석 보고서"
        story.append(Paragraph(title_text, self.styles['CustomTitle']))
        
        story.append(Spacer(1, 0.3*inch))
        
        subtitle_text = "Web Novel Genre & IP Expansion Trend Analysis Report"
        story.append(Paragraph(subtitle_text, self.styles['CustomSubtitle']))
        
        story.append(Spacer(1, 1*inch))
        
        total_genres = len(self.data_loader.get_period_stats('1개월', 'genre'))
        total_ips = len(self.data_loader.get_period_stats('1개월', 'ip'))
        
        info_text = f"""
        <para align=center>
        <font name="{KOREAN_FONT}" size=12>
        발행일: {today.strftime("%Y년 %m월 %d일")}<br/>
        분석 기간: 1개월, 3개월, 12개월<br/>
        분석 대상: {total_genres}개 장르 및 {total_ips}개 IP 확장 모델<br/>
        데이터 출처: Google Trends<br/>
        </font>
        </para>
        """
        story.append(Paragraph(info_text, self.styles['KoreanBody']))
        
        story.append(PageBreak())
        
        return story
    
    def create_executive_summary(self):
        """핵심 요약"""
        story = []
        
        story.append(Paragraph("핵심 요약 (Executive Summary)", self.styles['SectionTitle']))
        story.append(Spacer(1, 0.2*inch))
        
        genre_stats_1m = self.data_loader.get_period_stats('1개월', 'genre')
        ip_stats_1m = self.data_loader.get_period_stats('1개월', 'ip')
        
        if not genre_stats_1m.empty:
            top_genre = genre_stats_1m.iloc[0]
            
            summary_text = f"""
            <para align=left>
            <font name="{KOREAN_FONT}" size=12>
            <b>1. 이달의 가장 뜨거운 장르</b><br/>
            <font color="#d84315">■ {top_genre['항목']}</font> 장르가 평균 검색 관심도 
            <b>{top_genre['평균']:.2f}</b>로 1위를 기록했습니다.<br/>
            최대 관심도 <b>{top_genre['최대']}</b>, 최소 <b>{top_genre['최소']}</b>로 
            {"높은 변동성" if top_genre['표준편차'] > 5 else "안정적인 추세"}를 보이고 있습니다.<br/>
            <br/>
            </font>
            </para>
            """
            story.append(Paragraph(summary_text, self.styles['KoreanBody']))
        
        if not genre_stats_1m.empty:
            top3 = self.extractor.get_top_genres(genre_stats_1m, 3)
            top3_str = "', '".join(top3)
            
            top3_text = f"""
            <para align=left>
            <font name="{KOREAN_FONT}" size=12>
            <b>2. 현재 시장을 주도하는 장르</b><br/>
            상위 3개 장르는 '<b>{top3_str}</b>'로, 이들이 현재 웹소설 시장의 
            트렌드를 주도하고 있습니다.<br/>
            <br/>
            </font>
            </para>
            """
            story.append(Paragraph(top3_text, self.styles['KoreanBody']))
        
        if not ip_stats_1m.empty:
            top_ip = ip_stats_1m.iloc[0]
            
            ip_text = f"""
            <para align=left>
            <font name="{KOREAN_FONT}" size=12>
            <b>3. 가장 활발한 IP 확장 모델</b><br/>
            <font color="#1976d2">■ {top_ip['항목']}</font>가 평균 관심도 
            <b>{top_ip['평균']:.2f}</b>로 가장 높은 수치를 기록했습니다.<br/>
            """
            
            ip_ranking = []
            for idx, row in ip_stats_1m.iterrows():
                ip_ranking.append(f"{idx+1}위 {row['항목']} ({row['평균']:.1f})")
            
            ip_text += f"전체 순위: {', '.join(ip_ranking)}<br/>"
            ip_text += """
            <br/>
            </font>
            </para>
            """
            story.append(Paragraph(ip_text, self.styles['KoreanBody']))
        
        if not genre_stats_1m.empty:
            market = self.extractor.generate_market_summary(genre_stats_1m)
            
            market_text = f"""
            <para align=left>
            <font name="{KOREAN_FONT}" size=12>
            <b>4. 시장 전체 분석</b><br/>
            총 <b>{market['total_genres']}개</b> 장르를 분석한 결과, 
            평균 검색 관심도는 <b>{market['avg_interest']:.2f}</b>입니다.<br/>
            """
            
            if market['high_volatility']:
                vol_str = "', '".join(market['high_volatility'])
                market_text += f"변동성이 큰 장르는 '<b>{vol_str}</b>'로, 시장 변화에 민감하게 반응하고 있습니다.<br/>"
            
            market_text += """
            </font>
            </para>
            """
            story.append(Paragraph(market_text, self.styles['KoreanBody']))
        
        story.append(PageBreak())
        
        return story
    
    def create_period_analysis(self, period_name):
        """기간별 분석"""
        story = []
        
        section_titles = {
            '1개월': '섹션 1: [1개월] 단기 시장 집중 분석',
            '3개월': '섹션 2: [3개월] 중기 트렌드 변화',
            '12개월': '섹션 3: [12개월] 장기 스테디셀러 분석'
        }
        
        story.append(Paragraph(section_titles.get(period_name, f'{period_name} 분석'), 
                              self.styles['SectionTitle']))
        story.append(Spacer(1, 0.15*inch))
        
        genre_stats = self.data_loader.get_period_stats(period_name, 'genre')
        ip_stats = self.data_loader.get_period_stats(period_name, 'ip')
        changes = self.data_loader.get_changes(period_name, 'genre')
        
        if not genre_stats.empty:
            story.append(Paragraph(f"{period_name} 장르 순위 (전체 {len(genre_stats)}개)", 
                                  self.styles['SubsectionTitle']))
            
            table_data = [['순위', '장르', '평균', '최대', '최소', '표준편차']]
            
            for idx, row in genre_stats.head(10).iterrows():
                table_data.append([
                    str(idx + 1),
                    row['항목'],
                    f"{row['평균']:.2f}",
                    str(int(row['최대'])),
                    str(int(row['최소'])),
                    f"{row['표준편차']:.2f}"
                ])
            
            genre_table = Table(table_data, colWidths=[0.6*inch, 1.2*inch, 0.9*inch, 
                                                       0.9*inch, 0.9*inch, 1*inch])
            genre_table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#1976d2')),
                ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
                ('FONTNAME', (0, 0), (-1, 0), KOREAN_FONT),
                ('FONTSIZE', (0, 0), (-1, 0), 11),
                ('FONTNAME', (0, 1), (-1, -1), KOREAN_FONT),
                ('FONTSIZE', (0, 1), (-1, -1), 9),
                ('BOTTOMPADDING', (0, 0), (-1, 0), 10),
                ('GRID', (0, 0), (-1, -1), 0.5, colors.grey),
                ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor('#f5f5f5')]),
            ]))
            
            story.append(genre_table)
            story.append(Spacer(1, 0.2*inch))
        
        # 그래프 삽입 (순위 그래프만)
        ranking_graph = f"장르_ranking_{period_name.replace('개월', 'm')}.png"
        if os.path.exists(ranking_graph):
            story.append(Paragraph(f"{period_name} 장르 순위 시각화", self.styles['SubsectionTitle']))
            img = Image(ranking_graph, width=5.5*inch, height=3.2*inch)
            story.append(img)
            story.append(Spacer(1, 0.2*inch))
        
        if not ip_stats.empty:
            story.append(Paragraph(f"{period_name} IP 확장 현황", self.styles['SubsectionTitle']))
            
            ip_table_data = [['순위', 'IP 확장 모델', '평균', '최대', '최소']]
            
            for idx, row in ip_stats.iterrows():
                ip_table_data.append([
                    str(idx + 1),
                    row['항목'],
                    f"{row['평균']:.2f}",
                    str(int(row['최대'])),
                    str(int(row['최소']))
                ])
            
            ip_table = Table(ip_table_data, colWidths=[0.8*inch, 1.8*inch, 1*inch, 1*inch, 1*inch])
            ip_table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#0288d1')),
                ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
                ('FONTNAME', (0, 0), (-1, 0), KOREAN_FONT),
                ('FONTSIZE', (0, 0), (-1, 0), 11),
                ('FONTNAME', (0, 1), (-1, -1), KOREAN_FONT),
                ('FONTSIZE', (0, 1), (-1, -1), 9),
                ('BOTTOMPADDING', (0, 0), (-1, 0), 10),
                ('GRID', (0, 0), (-1, -1), 0.5, colors.grey),
                ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor('#e3f2fd')]),
            ]))
            
            story.append(ip_table)
            story.append(Spacer(1, 0.2*inch))
            
            # IP 확장 그래프도 추가
            ip_ranking_graph = f"IP확장_ranking_{period_name.replace('개월', 'm')}.png"
            if os.path.exists(ip_ranking_graph):
                story.append(Paragraph(f"{period_name} IP 확장 순위", self.styles['SubsectionTitle']))
                img = Image(ip_ranking_graph, width=5.5*inch, height=3.2*inch)
                story.append(img)
                story.append(Spacer(1, 0.2*inch))
        
        if not genre_stats.empty:
            top3 = self.extractor.get_top_genres(genre_stats, 3)
            top3_str = "', '".join(top3)
            
            avg_of_top3 = genre_stats.head(3)['평균'].mean()
            total_avg = genre_stats['평균'].mean()
            concentration = (avg_of_top3 / total_avg - 1) * 100 if total_avg > 0 else 0
            
            insight_text = f"""
            <para align=left>
            <font name="{KOREAN_FONT}" size=11>
            <b>[INSIGHT] {period_name} 핵심 인사이트:</b><br/>
            '{top3_str}' 장르가 상위권을 차지하고 있습니다.<br/>
            상위 3개 장르의 평균 관심도({avg_of_top3:.2f})는 전체 평균({total_avg:.2f})보다 
            <b>{abs(concentration):.1f}%</b> {"높아" if concentration > 0 else "낮아"} 
            {"시장 집중도가 높은" if concentration > 0 else "고른 분포를 보이는"} 상황입니다.<br/>
            """
            
            if not changes.empty and period_name != '1개월':
                growth_items = changes[changes['증감률(%)'] > 10]
                if not growth_items.empty:
                    growth_str = "', '".join(growth_items['항목'].tolist())
                    insight_text += f"<br/><b>급성장 장르:</b> '{growth_str}'가 두드러진 성장을 보이고 있습니다.<br/>"
            
            insight_text += """
            </font>
            </para>
            """
            story.append(Paragraph(insight_text, self.styles['InsightBox']))
        
        story.append(PageBreak())
        
        return story
    
    def create_growth_analysis(self):
        """증감률 분석"""
        story = []
        
        story.append(Paragraph("섹션 4: 기간별 증감 및 성장률 분석", self.styles['SectionTitle']))
        story.append(Spacer(1, 0.15*inch))
        
        genre_changes = self.data_loader.changes_data.get('genre', pd.DataFrame())
        ip_changes = self.data_loader.changes_data.get('ip', pd.DataFrame())
        
        if not genre_changes.empty:
            changes_3m = genre_changes[genre_changes['기간'] == '3개월'].copy()
            
            if not changes_3m.empty:
                story.append(Paragraph("장르별 증감률 분석 (3개월 vs 1개월)", 
                                      self.styles['SubsectionTitle']))
                
                table_data = [['순위', '장르', '현재평균', '증감량', '증감률(%)', '상태']]
                
                sorted_changes = changes_3m.sort_values('증감률(%)', ascending=False)
                
                for idx, row in sorted_changes.head(8).iterrows():
                    status = "[HOT] 급등" if row['증감률(%)'] > 20 else ("[UP] 상승" if row['증감률(%)'] > 0 else ("[DOWN] 하락" if row['증감률(%)'] < -20 else "-> 유지"))
                    
                    table_data.append([
                        str(len(table_data)),
                        row['항목'],
                        f"{row['평균']:.2f}",
                        f"{row['증감량']:+.2f}",
                        f"{row['증감률(%)']:+.1f}%",
                        status
                    ])
                
                change_table = Table(table_data, colWidths=[0.5*inch, 1.3*inch, 0.9*inch, 
                                                            0.9*inch, 1*inch, 0.9*inch])
                change_table.setStyle(TableStyle([
                    ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#d84315')),
                    ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                    ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
                    ('FONTNAME', (0, 0), (-1, 0), KOREAN_FONT),
                    ('FONTSIZE', (0, 0), (-1, 0), 10),
                    ('FONTNAME', (0, 1), (-1, -1), KOREAN_FONT),
                    ('FONTSIZE', (0, 1), (-1, -1), 9),
                    ('BOTTOMPADDING', (0, 0), (-1, 0), 10),
                    ('GRID', (0, 0), (-1, -1), 0.5, colors.grey),
                    ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor('#ffebee')]),
                ]))
                
                story.append(change_table)
                story.append(Spacer(1, 0.2*inch))
            
            top_growth = self.extractor.get_top_growth(changes_3m, 3)
            top_decline = self.extractor.get_top_decline(changes_3m, 3)
            
            if not top_growth.empty:
                max_growth = top_growth.iloc[0]
                
                highlight_text = f"""
                <para align=left>
                <font name="{KOREAN_FONT}" size=11>
                <b>[INFO] 증감률 분석 결과:</b><br/>
                <br/>
                <b>■ 최대 성장 장르:</b> '<font color="#d84315">{max_growth['항목']}</font>'<br/>
                - 증감률: <b>{max_growth['증감률(%)']:+.1f}%</b><br/>
                - 현재 평균: {max_growth['평균']:.2f}<br/>
                - 증감량: {max_growth['증감량']:+.2f}<br/>
                """
                
                if not top_decline.empty:
                    max_decline = top_decline.iloc[0]
                    highlight_text += f"""
                    <br/>
                    <b>■ 최대 감소 장르:</b> '<font color="#1565c0">{max_decline['항목']}</font>'<br/>
                    - 증감률: <b>{max_decline['증감률(%)']:+.1f}%</b><br/>
                    - 현재 평균: {max_decline['평균']:.2f}<br/>
                    """
                
                highlight_text += """
                </font>
                </para>
                """
                story.append(Paragraph(highlight_text, self.styles['InsightBox']))
                story.append(Spacer(1, 0.2*inch))
        
        if not ip_changes.empty:
            ip_changes_3m = ip_changes[ip_changes['기간'] == '3개월'].copy()
            
            if not ip_changes_3m.empty:
                story.append(Paragraph("IP 확장 증감률 분석 (3개월 vs 1개월)", 
                                      self.styles['SubsectionTitle']))
                
                ip_table_data = [['순위', 'IP 모델', '현재평균', '증감량', '증감률(%)']]
                
                sorted_ip = ip_changes_3m.sort_values('증감률(%)', ascending=False)
                
                for idx, row in sorted_ip.iterrows():
                    ip_table_data.append([
                        str(len(ip_table_data)),
                        row['항목'],
                        f"{row['평균']:.2f}",
                        f"{row['증감량']:+.2f}",
                        f"{row['증감률(%)']:+.1f}%"
                    ])
                
                ip_change_table = Table(ip_table_data, colWidths=[0.6*inch, 1.5*inch, 1*inch, 
                                                                   1*inch, 1.2*inch])
                ip_change_table.setStyle(TableStyle([
                    ('BACKGROUND', (0, 0), (-1, 0), colors.HexColor('#0288d1')),
                    ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
                    ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
                    ('FONTNAME', (0, 0), (-1, 0), KOREAN_FONT),
                    ('FONTSIZE', (0, 0), (-1, 0), 11),
                    ('FONTNAME', (0, 1), (-1, -1), KOREAN_FONT),
                    ('FONTSIZE', (0, 1), (-1, -1), 9),
                    ('BOTTOMPADDING', (0, 0), (-1, 0), 10),
                    ('GRID', (0, 0), (-1, -1), 0.5, colors.grey),
                    ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.HexColor('#e1f5fe')]),
                ]))
                
                story.append(ip_change_table)
        
        # 비교 그래프 삽입
        if os.path.exists('genre_period_comparison.png'):
            story.append(Spacer(1, 0.2*inch))
            story.append(Paragraph("기간별 장르 트렌드 비교", self.styles['SubsectionTitle']))
            img = Image('genre_period_comparison.png', width=5.5*inch, height=3.2*inch)
            story.append(img)
        
        if os.path.exists('ip_period_comparison.png'):
            story.append(Spacer(1, 0.2*inch))
            story.append(Paragraph("기간별 IP 확장 트렌드 비교", self.styles['SubsectionTitle']))
            img = Image('ip_period_comparison.png', width=5.5*inch, height=3.2*inch)
            story.append(img)
        
        story.append(PageBreak())
        
        return story
    
    def create_conclusion(self):
        """결론"""
        story = []
        
        story.append(Paragraph("결론 및 향후 전망", self.styles['SectionTitle']))
        story.append(Spacer(1, 0.15*inch))
        
        stats_1m = self.data_loader.get_period_stats('1개월', 'genre')
        stats_12m = self.data_loader.get_period_stats('12개월', 'genre')
        ip_1m = self.data_loader.get_period_stats('1개월', 'ip')
        changes = self.data_loader.changes_data.get('genre', pd.DataFrame())
        
        if not stats_1m.empty and not stats_12m.empty:
            top5_1m = set(stats_1m.head(5)['항목'].tolist())
            top5_12m = set(stats_12m.head(5)['항목'].tolist())
            consistent = list(top5_1m & top5_12m)
            consistent_str = "', '".join(consistent) if consistent else "없음"
            
            conclusion_text = f"""
            <para align=left>
            <font name="{KOREAN_FONT}" size=12>
            <b>1. 종합 의견</b><br/>
            <br/>
            웹소설 시장 분석 결과, 단기(1개월)와 장기(12개월) 모두에서 
            상위권을 유지하는 장르는 '<b>{consistent_str}</b>'입니다.<br/>
            이들은 시장의 핵심 장르로 안정적인 독자층을 확보하고 있습니다.<br/>
            <br/>
            </font>
            </para>
            """
            story.append(Paragraph(conclusion_text, self.styles['KoreanBody']))
        
        if not changes.empty:
            changes_3m = changes[changes['기간'] == '3개월']
            growth_genres = changes_3m[changes_3m['증감률(%)'] > 20]
            
            if not growth_genres.empty:
                growth_str = "', '".join(growth_genres['항목'].tolist())
                
                short_term_text = f"""
                <para align=left>
                <font name="{KOREAN_FONT}" size=12>
                <b>2. 단기 전략 (1-3개월)</b><br/>
                <br/>
                <b>급성장 장르 집중:</b><br/>
                • '<b>{growth_str}</b>' 장르가 높은 성장률을 보이고 있어, 
                신속한 콘텐츠 기획이 필요합니다.<br/>
                • 해당 장르의 신인 작가 발굴 및 기존 작가 섭외를 우선적으로 진행<br/>
                • 마케팅 리소스를 성장 장르에 집중 배분<br/>
                <br/>
                </font>
                </para>
                """
                story.append(Paragraph(short_term_text, self.styles['KoreanBody']))
        
        if not stats_12m.empty:
            steady = stats_12m.head(3)
            steady_str = "', '".join(steady['항목'].tolist())
            
            long_term_text = f"""
            <para align=left>
            <font name="{KOREAN_FONT}" size=12>
            <b>3. 장기 전략 (12개월 이상)</b><br/>
            <br/>
            <b>안정적 스테디셀러 유지:</b><br/>
            • '<b>{steady_str}</b>' 장르는 12개월 평균 상위권으로 
            장기 시리즈물 기획에 적합합니다.<br/>
            • 이들 장르의 IP 확장(웹툰, 드라마, 게임) 사전 검토<br/>
            • 안정적 수익 창출을 위한 라인업 구축<br/>
            <br/>
            </font>
            </para>
            """
            story.append(Paragraph(long_term_text, self.styles['KoreanBody']))
        
        if not ip_1m.empty:
            top_ip = ip_1m.iloc[0]
            
            ip_strategy_text = f"""
            <para align=left>
            <font name="{KOREAN_FONT}" size=12>
            <b>4. IP 확장 전략</b><br/>
            <br/>
            현재 '<b>{top_ip['항목']}</b>'가 가장 활발한 IP 확장 모델입니다 (평균 {top_ip['평균']:.2f}).<br/>
            • 1차: {top_ip['항목']} 우선 추진<br/>
            """
            
            for idx, row in ip_1m.iloc[1:].iterrows():
                ip_strategy_text += f"• {idx+1}차: {row['항목']} (평균 {row['평균']:.2f})<br/>"
            
            ip_strategy_text += """
            <br/>
            단계적 IP 확장으로 리스크를 분산하고 시장 반응을 확인하면서 진행하는 것을 권장합니다.<br/>
            </font>
            </para>
            """
            story.append(Paragraph(ip_strategy_text, self.styles['KoreanBody']))
        
        story.append(Spacer(1, 0.3*inch))
        
        if not stats_1m.empty and not ip_1m.empty:
            top_genre = stats_1m.iloc[0]['항목']
            top_ip = ip_1m.iloc[0]['항목']
            
            recommendation_text = f"""
            <para align=center>
            <font name="{KOREAN_FONT}" size=12>
            <b>* 데이터 기반 핵심 권장사항 *</b><br/>
            <br/>
            1. <b>{top_genre}</b> 장르 작품 우선 확보 (현재 1위)<br/>
            2. <b>{top_ip}</b> 우선 추진<br/>
            3. 급성장 장르 모니터링 시스템 구축<br/>
            4. 안정 장르와 성장 장르의 균형있는 포트폴리오 구성<br/>
            </font>
            </para>
            """
            story.append(Paragraph(recommendation_text, self.styles['InsightBox']))
        
        return story
    
    def generate(self):
        """PDF 생성"""
        print("\n" + "=" * 80)
        print("PART 2: PDF 보고서 생성 시작")
        print("=" * 80)
        
        self.data_loader.load_all_data()

        today = datetime.now()
        pdf_filename = f"{today.year}년 {today.month:02d}월 IP 트랜드 분석 보고서.pdf"
        
        doc = SimpleDocTemplate(
            pdf_filename,
            pagesize=A4,
            rightMargin=72,
            leftMargin=72,
            topMargin=72,
            bottomMargin=18,
        )
        
        story = []
        
        print("\n[1/6] 표지 생성 중...")
        story.extend(self.create_cover_page())
        
        print("[2/6] 핵심 요약 생성 중...")
        story.extend(self.create_executive_summary())
        
        print("[3/6] 기간별 분석 섹션 생성 중...")
        for period in ['1개월', '3개월', '12개월']:
            story.extend(self.create_period_analysis(period))
        
        print("[4/6] 증감률 분석 섹션 생성 중...")
        story.extend(self.create_growth_analysis())
        
        print("[5/6] 결론 및 전망 생성 중...")
        story.extend(self.create_conclusion())
        
        print("[6/6] PDF 문서 빌드 중...")
        doc.build(story)
        
        print("\n" + "=" * 80)
        print(f"[OK] 보고서 생성 완료: {pdf_filename}")
        print("=" * 80)
        print(f"\n파일 위치: {os.path.abspath(pdf_filename)}")
        print(f"파일 크기: {os.path.getsize(pdf_filename) / 1024:.2f} KB")
        
        return pdf_filename

############################################
# 메인 실행
############################################

def main():
    """통합 메인 함수"""
    print("\n" + "=" * 80)
    print("웹소설 트렌드 분석 및 PDF 보고서 자동 생성 시스템")
    print("=" * 80)
    print("\n[실행 프로세스]")
    print("  1단계: Google Trends 데이터 수집 및 분석")
    print("  2단계: PDF 보고서 자동 생성")
    print("\n시작 시간:", datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
    print("=" * 80)
    
    try:
        # PART 1: 데이터 분석
        analyzer = TrendAnalyzer()
        analysis_success = analyzer.run_analysis()
        
        if not analysis_success:
            print("\n[FAIL] 데이터 분석 실패 - PDF 생성 중단")
            return
        
        # 실패한 항목 재시도
        retry_success = analyzer.retry_failed_items()
        
        if not retry_success:
            print("\n[WARNING] 일부 항목 수집 실패했지만 PDF 생성을 진행합니다...")
        
        # PART 2: PDF 생성
        data_loader = TrendDataLoader()
        extractor = InsightExtractor()
        pdf_generator = PDFReportGenerator(data_loader, extractor)
        pdf_file = pdf_generator.generate()
        
        # 최종 요약
        print("\n" + "=" * 80)
        print("[SUCCESS] 전체 프로세스 완료!")
        print("=" * 80)
        
        print("\n[INFO] 생성된 파일 목록:")
        print("\n  [데이터 파일]")
        print("    - genre_data_*.csv (기간별 원본 데이터)")
        print("    - genre_stats_*.csv (기간별 통계)")
        print("    - genre_changes_analysis.csv (증감량 분석)")
        print("    - ip_expansion_data_*.csv (IP 확장 원본)")
        print("    - ip_expansion_stats_*.csv (IP 확장 통계)")
        
        print("\n  [그래프 파일]")
        print("    - 장르_trends_*.png (장르 추이)")
        print("    - 장르_ranking_*.png (장르 순위)")
        print("    - IP확장_trends_*.png (IP 추이)")
        print("    - IP확장_ranking_*.png (IP 순위)")
        print("    - genre_period_comparison.png (기간별 비교)")
        
        print(f"\n  [PDF 보고서]")
        print(f"     {pdf_file}")
        
        print("\n" + "=" * 80)
        print("종료 시간:", datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
        print("=" * 80)
        
    except Exception as e:
        print(f"\n[FAIL] 오류 발생: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    main()
