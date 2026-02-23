# AI Novel Platform Backend (IP-Ergo-Sum)
AIVLE SCHOOL 8기 Big Project - AI 기반 웹소설 창작 및 IP 확장 지원 시스템 백엔드 리포지토리입니다.

## 🔗 Repository Links
[![Frontend](https://img.shields.io/badge/Frontend-Repository-blue?style=for-the-badge&logo=github)](https://github.com/Joyusong/ai0917-kt-aivle-shool-8th-bigproject-frontend)
[![AI](https://img.shields.io/badge/AI-Repository-orange?style=for-the-badge&logo=github)](https://github.com/Joyusong/kt_aivle_school_8th_bigpriject-ai)


## 📖 프로젝트 개요

이 프로젝트는 AI 기술을 활용하여 웹소설 창작을 지원하고, IP(Intellectual Property) 확장을 체계적으로 관리할 수 있는 통합 플랫폼의 백엔드 서버입니다. 관리자(Admin), 운영자(Manager), 작가(Author) 역할에 따른 API를 제공하며, 외부 AI 서버와의 통신을 통해 집필 보조, 설정집 관리, IP 확장 제안서 자동 생성 등의 기능을 지원합니다.

## ✨ 주요 기능

- **역할별 API (Role-Based API)**:
  - **Admin (관리자)**: 시스템 모니터링, 사용자 권한 관리, DAU 집계, 시스템 로그 및 공지 관리
  - **Manager (운영자)**: 작가 및 작품 관리, IP 확장(OSMU) 제안 생성 및 상태 관리, IP 트렌드 리포트
  - **Author (작가)**: 원고(에피소드) CRUD, AI 설정집(Lorebook) 관리, 원고 분석 요청, IP 제안 조회 및 댓글

- **AI 연동 (AI Integration)**:
  - WebFlux 기반 외부 AI 서버(AiClient) 비동기 통신
  - 설정집 충돌 감지, 원고 분석(관계도/타임라인), IP 확장 제안서 자동 생성

- **인증 및 보안**:
  - JWT(Access/Refresh Token) 기반 인증 + Redis 토큰 블랙리스트
  - 네이버 OAuth2 소셜 로그인
  - Spring Security 기반 RBAC 접근 제어

- **스케줄러**:
  - DAU 집계, 시스템 메트릭 수집, IP 트렌드 리포트 자동 생성
  - 탈퇴/권한 회수 7일 유예 후 사용자 데이터 자동 정리

## 🛠 기술 스택

| 분류 | 기술 |
|------|------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.5.x |
| **ORM** | Spring Data JPA |
| **Security** | Spring Security, JWT (jjwt 0.12.5), OAuth2 Client |
| **Database** | PostgreSQL |
| **Cache / Session** | Redis (Spring Data Redis) |
| **Async HTTP** | Spring WebFlux (WebClient) |
| **AOP** | Spring AOP (로깅, 알림 Aspect) |
| **Mail** | Spring Mail |
| **PDF 처리** | Apache PDFBox 2.0.30 |
| **Validation** | Spring Validation |
| **Build** | Gradle |

## 📂 패키지 구조

```
com.aivle.ai0917.ipai
├── IpaiApplication.java
│
├── domain
│   ├── admin
│   │   ├── access          # 사용자 권한 관리 (조회, 생성, 수정, 비활성화)
│   │   ├── dashboard       # 시스템 모니터링 (DAU, 서버 상태, 시스템 로그)
│   │   └── info            # 관리자 공지 관리
│   │
│   ├── author
│   │   ├── analyze         # AI 원고 분석 (관계도, 타임라인)
│   │   ├── dashboard       # 작가 대시보드 통계
│   │   ├── episodes        # 원고(에피소드) CRUD 및 AI 집필 보조
│   │   ├── info            # 작가 공지 조회
│   │   ├── invitecode      # 초대 코드 관리
│   │   ├── ipextcomment    # IP 확장 제안 댓글 (작가 측)
│   │   ├── lorebook        # AI 설정집 관리 및 충돌 감지
│   │   ├── manager         # 작가-운영자 연결 정보 조회
│   │   └── works           # 작품(Work) CRUD
│   │
│   ├── manager
│   │   ├── authors         # 작가 관리 (목록, 상세, 매칭)
│   │   ├── dashboard       # 운영자 대시보드 통계
│   │   ├── info            # 운영자 공지 관리
│   │   ├── ipext           # IP 확장 제안 생성 및 상태 관리
│   │   ├── ipextcomment    # IP 확장 제안 댓글 (운영자 측)
│   │   └── iptrend         # IP 트렌드 리포트
│   │
│   ├── notice              # 공통 공지사항
│   │
│   ├── user                # 인증 (로그인, 회원가입, 이메일 인증, 비밀번호 변경)
│   │
│   └── test                # 개발용 테스트 엔드포인트
│
├── global
│   ├── aspect              # AOP (LoggingAspect, NotificationAspect)
│   ├── config              # 설정 (Security, Mail, Scheduler, WebAI, CORS 등)
│   ├── csrf                # CSRF 토큰 발급
│   ├── security
│   │   ├── interceptor     # LastActivityInterceptor (활동 시간 추적)
│   │   ├── jwt             # JWT 발급/검증, @CurrentUserId 어노테이션
│   │   └── token           # Redis 토큰 블랙리스트
│   └── utils               # 공통 유틸 (Base62, FileStore)
│
└── infra
    └── naver               # 네이버 OAuth2 연동 (토큰 교환, 프로필 조회, 이메일 인증)
```

> 각 도메인 패키지는 `controller / dto / model / repository / service` 레이어드 구조를 따릅니다.

## 🔒 보안 및 인증

- **JWT 인증**: Access Token은 Authorization 헤더로 관리, Refresh Token은 HttpOnly Cookie로 관리하며 Redis에 저장하여 탈취 시 즉시 무효화 가능
- **토큰 블랙리스트**: 로그아웃 시 Access Token을 Redis 블랙리스트에 등록하여 재사용 방지
- **네이버 OAuth2**: State 토큰 검증을 통한 CSRF 방지, 이메일 인증 병행 지원
- **RBAC**: Spring Security + JWT Claim 기반의 역할별 엔드포인트 접근 제어 (Admin / Manager / Author)
- **AOP 로깅**: `LoggingAspect`를 통한 API 호출 이력 자동 기록
- **활동 추적**: `LastActivityInterceptor`로 마지막 활동 시간 갱신 및 DAU 집계 연동
- **7일 유예 처리**: 권한 회수 및 탈퇴 요청 시 즉시 삭제하지 않고 스케줄러로 유예 기간 후 자동 정리

## 🚀 시작하기 (Getting Started)

### 사전 요구사항
- Java 17
- PostgreSQL
- Redis

### 환경 설정

`src/main/resources/` 아래 환경별 설정 파일을 구성합니다.

```
resources/
├── application.yaml          # 공통 설정
├── application-local.yaml    # 로컬 개발 환경
└── application-secret.yaml   # 민감 정보 (DB, Redis, JWT Secret, OAuth Key 등)
```

> `application-secret.yaml`은 `.gitignore`에 포함하여 버전 관리에서 제외합니다.

### 실행

```bash
./gradlew bootRun
```

### 빌드

```bash
./gradlew build
```
<HR>


# PostgreSQL 17 + pgvector 기반 벡터 데이터베이스 구축 가이드

본 가이드는 **Windows 환경**에서 **Docker**를 사용하여 **PostgreSQL 17 + pgvector**를 구축하고,  
**Tailscale**을 통해 팀원과 서버를 공유하는 **전체 실무 절차**를 다룹니다.


## 1. 사전 준비 (Prerequisites)

안정적인 구동을 위해 아래 환경이 **사전에 반드시 구축**되어 있어야 합니다.

- **Docker Desktop (Windows)**
  - 설치 및 실행 상태
  - Settings → General → **Use the WSL 2 based engine** 체크
- **WSL 2**
  - Docker Desktop과 연동되어 있어야 함
- **Tailscale**
  - 서버(호스트 PC)와 팀원 클라이언트 모두 로그인 상태


## 2. Docker 배포 및 서버 실행 (Deployment)

PostgreSQL 17과 `pgvector` 확장이 포함된 공식 이미지를 사용합니다.  
데이터 유실 방지와 **HNSW 인덱스 성능 최적화** 설정을 포함합니다.

### 2-1. 이미지 다운로드
 
     docker pull pgvector/pgvector:pg17

### 2-2. 컨테이너 실행
⚠️ HNSW 인덱스 빌드 시 공유 메모리 부족 오류 방지를 위해
--shm-size=1g 옵션은 반드시 포함해야 합니다.
  
    docker run -d
    --name postgres17-vector
    -p 5432:5432
    --shm-size=1g
    -v pgdata:/var/lib/postgresql/data
    -e POSTGRES_USER=postgres
    -e POSTGRES_PASSWORD=postgres
    -e POSTGRES_DB=vector_db
    pgvector/pgvector:pg17


### 2-3. 옵션 설명

  - --shm-size=1g : HNSW 인덱스 오류 방지 (HNSW 병렬 인덱스 생성 시 발생하는 shared memory 오류 방지 (필수))
  - -v pgdata:/var/lib/postgresql/data : 데이터 유지 (컨테이너 삭제 후에도 데이터 유지)
  - -p 5432:5432 : 포트 매핑 (호스트 ↔ 컨테이너 포트 매핑 (외부 접속 허용))

## 3. 데이터베이스 초기 설정
  
  컨테이너 실행 후, PostgreSQL 내부에서 pgvector 확장을 활성화해야 합니다.
  
  ### 3-1. 컨테이너 내부 접속
  
      docker exec -it postgres17-vector psql -U postgres -d vector_db

  ### 3-2. pgvector 확장 활성화
  
      CREATE EXTENSION IF NOT EXISTS vector;


  ### 3-3. 설치 확인
  
      \dx

  확장 목록에 **vector**가 표시되면 정상 설치 완료

## 4. 외부 접속

Tailscale의 가상 사설망 IP를 사용하여 팀원들이 PostgreSQL 서버에 접속합니다.

  ### 4-1. 접속 정보 (Connection Info)
<table> <thead> <tr> <th>항목</th> <th>설정값</th> </tr> </thead> <tbody> <tr> <td>Host (IP)</td> <td>100.95.214.66 (Tailscale IP)</td> </tr> <tr> <td>Port</td> <td>5432</td> </tr> <tr> <td>Database</td> <td>vector_db</td> </tr> <tr> <td>Username</td> <td>postgres (또는 지정된 계정)</td> </tr> <tr> <td>Password</td> <td>postgres (또는 지정된 비밀번호)</td> </tr> </tbody> </table>

  ### 4-2. Spring Boot (JPA) 설정 예시
    
  application.yaml
    
      spring.datasource.url=jdbc:postgresql://100.95.214.66:5432/vector_db
      spring.datasource.username=postgres
      spring.datasource.password=postgres
      spring.datasource.driver-class-name=org.postgresql.Driver


## 5. 트러블슈팅

### 1. 포트 충돌

- 현상

  - 컨테이너 실행 실패
  - authentication failed 오류

- 원인

  - Windows 로컬에 설치된 PostgreSQL이 이미 5432 포트 점유

- 해결

  - Windows 서비스에서 로컬 PostgreSQL 중지
  - 또는 Docker 컨테이너 포트를 다른 포트로 매핑

### 2. pg_hba.conf 인증

컨테이너 내부 파일 수정
- 현상: no pg_hba.conf entry for host 오류
- 원인: Tailscale IP 대역이 허용되지 않음
- 해결:
    
      vi /var/lib/postgresql/data/pg_hba.conf
      host all all 100.64.0.0/10 md5
      docker restart postgres17-vector


### 3. 데이터베이스 권한 문제


  - 현상 
    - permission denied for schema public

  - 해결
    
    - 관리자 계정으로 권한 부여

        - GRANT USAGE ON SCHEMA public TO team_user;
        - GRANT CREATE ON SCHEMA public TO team_user;


## 6. 주의 사항

- 개인 PC 기반 서버 → PC 종료 시 접속 차단
- 기본 postgres 계정 공유 금지 → 팀원별 계정 생성
