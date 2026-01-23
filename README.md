## 보은군 공고 알리미 Android 앱

보은군의 새로운 공고를 실시간으로 알려주는 Android 애플리케이션입니다.

### 주요 기능

1. **실시간 백그라운드 감시**
   - WorkManager를 사용하여 15분마다 자동으로 새 공고 확인
   - 앱이 종료되어도 백그라운드에서 계속 작동
   - 새 공고 발견 시 즉시 푸시 알림

2. **푸시 알림**
   - 새 공고 발견 시 상단바 알림 표시
   - 알림 클릭 시 해당 공고 페이지로 자동 이동
   - 중요도 높음으로 설정된 알림 채널

3. **공고 목록 화면**
   - 깔끔한 카드 형태의 UI
   - 제목, 날짜, 카테고리 표시
   - 스와이프로 새로고침 기능
   - 공고 클릭 시 브라우저에서 상세 페이지 열기

### 기술 스택

- **언어**: Kotlin
- **아키텍처**: MVVM (Model-View-ViewModel)
- **라이브러리**:
  - Retrofit2 + OkHttp3: 네트워크 통신
  - Coroutines: 비동기 처리
  - ViewModel + LiveData: 반응형 UI
  - ViewBinding: 뷰 바인딩
  - WorkManager: 백그라운드 작업
  - RecyclerView + ListAdapter: 효율적인 리스트 표시
  - SwipeRefreshLayout: 당겨서 새로고침
  - Material Design Components: UI 디자인

### 프로젝트 구조

```
app/src/main/java/com/boeun/announcement/
├── data/                   # 데이터 모델
│   └── Announcement.kt
├── network/                # API 서비스
│   ├── ApiService.kt
│   └── RetrofitClient.kt
├── repository/             # 데이터 저장소
│   └── AnnouncementRepository.kt
├── viewmodel/              # ViewModel
│   └── AnnouncementViewModel.kt
├── ui/                     # UI 컴포넌트
│   ├── MainActivity.kt
│   └── AnnouncementAdapter.kt
├── worker/                 # 백그라운드 작업
│   ├── NoticeCheckWorker.kt
│   └── WorkManagerHelper.kt
└── utils/                  # 유틸리티
    ├── NotificationHelper.kt
    └── PreferenceHelper.kt
```

### 설정 방법

1. **API 서버 URL 수정**
   - `network/RetrofitClient.kt` 파일의 `BASE_URL`을 실제 서버 주소로 변경

2. **알림 권한**
   - Android 13 이상에서는 앱 실행 시 알림 권한 요청
   - 설정 > 앱 > 권한에서 수동으로 설정 가능

3. **백그라운드 작업 주기**
   - `worker/WorkManagerHelper.kt`에서 체크 주기 변경 가능 (기본 15분)

### 빌드 및 실행

```bash
# Gradle 빌드
./gradlew build

# 디버그 APK 생성
./gradlew assembleDebug

# 앱 설치 및 실행
./gradlew installDebug
```

### 주의사항

- 최소 SDK 버전: 24 (Android 7.0)
- 타겟 SDK 버전: 34 (Android 14)
- 인터넷 연결 필수
- 백그라운드 작업을 위해 배터리 최적화 예외 설정 권장

### 라이선스

이 프로젝트는 보은군 공고 알림 서비스를 위한 것입니다.
