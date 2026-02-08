package com.aivle.ai0917.ipai.domain.manager.ipext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 매체별 상세 설정을 담는 DTO 모음입니다.
 * 이 객체들은 DB의 'media_detail' 컬럼(jsonb)에 저장됩니다.
 */
public class IpMediaDetails {

    // 1. 웹툰 상세
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Webtoon {
        private ArtStyle artStyle;
        private DirectionPace directionPace;
        private EndingPoint endingPoint;
        private String colorTone;

        @Getter @AllArgsConstructor
        public enum ArtStyle {
            REALISTIC("실사체"),
            CASUAL_SD("캐주얼/SD"),
            MARTIAL_ARTS("무협/극화체"),
            AMERICAN_COMICS("미국 코믹스");
            private final String description;
        }
        @Getter @AllArgsConstructor
        public enum DirectionPace {
            FAST("빠른 전개"),
            EMOTIONAL("감정선 중심"),
            TENSION("긴장감 조성");
            private final String description;
        }
        @Getter @AllArgsConstructor
        public enum EndingPoint {
            CLIFFHANGER("절단신공"),
            EPISODIC("에피소드 완결형"),
            PREVIEW("다음화 예고 강조");
            private final String description;
        }
    }

    // 2. 드라마 상세
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Drama {
        private BroadcastStrategy broadcastStrategy;
        private EpisodeRuntime episodeRuntime;
        private String subElement;

        @Getter @AllArgsConstructor
        public enum BroadcastStrategy {
            SINGLE("단막극"),
            MULTI_SEASON("멀티시즌");
            private final String description;
        }
        @Getter @AllArgsConstructor
        public enum EpisodeRuntime {
            SITCOM_30("30분 시트콤"),
            STANDARD_60("60분 표준"),
            SPECIAL_80("80분이상 스페셜");
            private final String description;
        }
    }

    // 3. 영화 상세
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Movie {
        private Runtime runtime;
        private String colorTheme;
        private ThreeActFocus threeActFocus;

        @Getter @AllArgsConstructor
        public enum Runtime {
            COMPACT_90("90분 내외"),
            STANDARD_120("120분 표준"),
            EPIC_150("150분 이상");
            private final String description;
        }
        @Getter @AllArgsConstructor
        public enum ThreeActFocus {
            ACT1("1막: 설정"),
            ACT2("2막: 갈등"),
            ACT3("3막: 해결");
            private final String description;
        }
    }

    // 4. 게임 상세
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Game {
        private String gameGenre;
        private CoreFun coreFun;
        private PlatformBM platformBM;

        @Getter @AllArgsConstructor
        public enum CoreFun {
            GROWTH("성장/육성"),
            BATTLE("전투/경쟁"),
            COLLECTION("수집/도감"),
            STORY("스토리/선택");
            private final String description;
        }
        @Getter @AllArgsConstructor
        public enum PlatformBM {
            MOBILE_F2P("모바일 F2P"),
            PC_CONSOLE_PACKAGE("PC/콘솔 패키지");
            private final String description;
        }
    }

    // 5. 스핀오프 상세
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class Spinoff {
        private Direction direction;
        private String mainCharacter;
        private SerializationPace serializationPace;

        @Getter @AllArgsConstructor
        public enum Direction {
            PREQUEL("프리퀄"),
            SEQUEL("시퀄"),
            SIDE_STORY("외전");
            private final String description;
        }
        @Getter @AllArgsConstructor
        public enum SerializationPace {
            WEEKLY("주간 연재"),
            BOOK("단행본"),
            OMNIBUS("옴니버스");
            private final String description;
        }
    }

    // 6. 상업 이미지 상세
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CommercialImage {
        private VisualFormat visualFormat;
        private String purpose;
        private String targetProduct;

        @Getter @AllArgsConstructor
        public enum VisualFormat {
            IMAGE_2D("2D 일러스트"),
            MODEL_3D("3D 모델링"),
            CHARACTER_SD("SD 캐릭터");
            private final String description;
        }
    }
}