# 일본여행지도 (JapanMap) — Android

iOS 앱(`/Users/yj.no14/Desktop/JapanMap`)의 Android 포팅. 갤럭시 등 안드로이드 기기에서 사용.

일본 47개 도도부현을 SVG 기반 인터랙티브 맵으로 표시하고, 사진을 올리면
회색 → 컬러로 바뀌며 자신만의 여행 지도를 완성하는 앱.

## 기술 스택

| 항목 | iOS | Android (본 프로젝트) |
|------|-----|----------------------|
| 언어/UI | Swift / UIKit | Kotlin / Jetpack Compose |
| 데이터 | SwiftData | Room |
| 사진 | PHPicker + FileSystem | PhotoPicker + 내부 저장소 + Coil |
| 지도 렌더 | CAShapeLayer | Compose Canvas |
| 아키텍처 | MVVM-C + Clean | MVVM + Clean (동일 사상) |
| DI | DIContainer | AppContainer (수동 DI) |

- minSdk 26 / targetSdk 34 / Kotlin 2.0 / Compose

## 아키텍처 (iOS와 동일한 레이어 분리)

```
presentation (Compose, ViewModel) → domain (순수 Kotlin) ← data (Room, JSON, File)
                          di (AppContainer = Composition root)
```

- `domain/` — Entity 5, Repository 인터페이스 4, UseCase 5. **Android 의존성 없음.**
- `data/` — Room DB(Trip/Photo cascade), assets JSON 로더, 사진 파일 저장, Mapper, Repository 구현.
- `presentation/` — 맵 컴포넌트, DesignSystem(컬러 토큰 1:1), Main/Detail/Editor/Gallery 화면.

## 재사용된 자산

iOS의 데이터 파일을 **그대로** 복사해서 사용합니다:
- `app/src/main/assets/prefectures.json` (47개 도도부현 SVG path)
- `app/src/main/assets/subregions/1..47.json` (하위 여행지)

컬러 토큰, 오키나와 인셋 변형(2배 확대·오프셋), hit-test(반경 25), 좁은 라벨 목록 등
시각 스펙도 iOS와 동일하게 이식했습니다.

## 빌드 / 실행

> ⚠️ 이 프로젝트는 소스만 생성되어 있습니다. **Gradle Wrapper JAR과 `local.properties`는
> Android Studio가 자동 생성**합니다. (생성 환경에 JDK/Android SDK가 없어 포함하지 못함)

1. **Android Studio**(Koala 이상 권장)로 `JapanMapAndroid` 폴더를 연다.
2. Android Studio가 Gradle Sync를 수행하며 wrapper와 SDK 경로를 자동 설정한다.
3. 에뮬레이터 또는 갤럭시 기기(개발자 모드 + USB 디버깅)를 연결하고 ▶ Run.

명령줄에서 하려면 (JDK 17 + Android SDK 설치 후):
```bash
cd JapanMapAndroid
gradle wrapper          # wrapper JAR 생성 (최초 1회)
./gradlew assembleDebug  # 또는 installDebug
```

## Phase 1~5 대응 현황

- ✅ 인터랙티브 47 도도부현 맵 (줌 1~3배, 더블탭 줌, 팬, hit-test, 라벨 줌 opacity)
- ✅ 방문 진행률 (n/47 + %)
- ✅ 도도부현 상세 — 여행 기록 목록 / 빈 상태 / 삭제
- ✅ 여행 기록 작성 — 사진 다중 선택, 여행지 선택, 기간 선택, 메모, canSave 검증
- ✅ 사진 갤러리 (페이저 + 핀치 줌)
- ✅ 오키나와 우하단 인셋 + 점선 테두리

## 후속 과제 (iOS의 Post-Phase 5)

- 실제 지리 지도(MapKit) 상세 화면 → Android는 Google Maps Compose 필요 (API 키 발급 후 추가)
- 다크 모드, 100% 달성 축하 애니메이션, 사진 reorder 등
