# MenuPlugin — 사용법 & 테스트 가이드

> 대상: 마인팜 백엔드 서버 운영자.
> 이 문서 하나로 **빌드 → 설치 → 설정 → 기능 테스트**까지 전부 진행할 수 있습니다.

---

## 1. 빌드 방법

### 준비물
| 항목 | 값 |
|---|---|
| JDK | 25 (Paper 26.x 기준. `pom.xml`의 `maven.compiler.release`와 일치해야 함) |
| Maven | 3.9+ (IntelliJ 내장 Maven 가능) |
| 인터넷 | `repo.papermc.io` 접근 필요 (paper-api 다운로드) |

### IntelliJ 에서 빌드
1. IntelliJ → `Open` → 압축 푼 폴더(`pom.xml`이 있는 폴더) 선택
2. 우측 Maven 탭 → `MenuPlugin` → `Lifecycle` → **`package`** 더블클릭
3. 성공 시 결과물: **`target/MenuPlugin.jar`** (접미사 없음 — `finalName` 고정됨)

### 터미널에서 빌드
```bash
mvn clean package
# → target/MenuPlugin.jar
```

### 대상 버전 바꾸기
`pom.xml` 상단 properties 수정:
```xml
<paper.version>26.2-R0.1-SNAPSHOT</paper.version>   <!-- 실험 -->
<!-- 안정 빌드로 바꾸려면: 26.1.2-R0.1-SNAPSHOT -->
<maven.compiler.release>25</maven.compiler.release>
```
> ⚠️ 이 프로젝트는 원격 환경(papermc 저장소 차단)에서 작성되어 **아직 실제 컴파일 검증 전**입니다.
> 첫 빌드에서 에러가 나오면 에러 메시지를 그대로 복사해 주세요 — 대부분 API 시그니처 한두 줄 수정으로 끝납니다.

---

## 2. 설치

1. `MenuPlugin.jar` 를 `/메뉴`가 열려야 하는 **모든 백엔드 서버**의 `plugins/` 폴더에 넣기
2. 서버 재시작 (최초 실행 시 `plugins/MenuPlugin/config.yml` + `menus/hub.yml` 자동 생성)
3. 콘솔에서 로드 메시지 확인:
   - 성공: `MenuPlugin - 연결 성공` (주황)
   - 실패: `MenuPlugin - 연결 실패` (빨강) + 스택트레이스
4. 선택 의존:
   - **WarpCraft** — `type: warp` 버튼이 동작하려면 필요 (없으면 클릭 시 `warp-unavailable` 메시지)
   - **Nexo** — `<glyph:...>` `<shift:...>` 태그·`nexo-item` 렌더용 (없으면 해당 태그만 무시되고 나머지 정상)

---

## 3. 명령어 / 열기 방법

| 방법 | 설명 |
|---|---|
| `/메뉴` `/menu` `/apsb` | 허브 메뉴 열기 (`menus/hub.yml`의 `open-commands`에서 변경 가능) |
| **Shift + F** | 스니크 상태에서 오프핸드 교체 키 → 메뉴 열림 (config `sneak-swap-open`) |
| `/메뉴리로드` | **OP 전용.** config + menus 전체 리로드. 비OP가 치면 "알 수 없는 명령어" 응답 + 탭완성에도 안 뜸 |

- 명령어는 **plugin.yml에 없고 런타임 동적 등록** → yml에서 이름을 바꾸고 `/메뉴리로드`만 하면 즉시 반영됩니다.
- 메뉴 열기에는 **쿨다운**(기본 3초)이 적용됩니다.

---

## 4. 메뉴/버튼 설정 사용법

### 새 메뉴 추가
1. `plugins/MenuPlugin/menus/jobmenu.yml` 처럼 파일 생성 (형식은 hub.yml과 동일)
2. 다른 메뉴 버튼에서 `type: open` + `open: jobmenu` 로 연결하거나, `open-commands`를 지정해 명령어로 직접 열기
3. `/메뉴리로드`

### 슬롯 지정 (한 버튼이 여러 칸 차지)
```yaml
slot: 36                     # 단일
slot: [3, 4, 12, 13]         # 나열
slot: "0-2, 9-11"            # 범위+묶음 = 0,1,2,9,10,11
slot: [ "0-2", "9-11", 44 ]  # 혼용
```
펼쳐진 모든 칸에 같은 아이템이 놓이고, 어느 칸을 눌러도 같은 동작이 실행됩니다.

### 버튼 타입별 작성법
```yaml
# ① 이동 (WarpCraft 위임 — warp 이름은 WarpCraft DB 등록명)
my-spawn:
  slot: "0-2, 9-11"
  item: { material: PAPER, custom-model-data: 20000, name: "<green><bold>스폰</bold></green>" }
  type: warp
  warp: spawn

# ② 명령어 실행
my-auction:
  slot: [7]
  item: { material: PAPER, name: "<yellow>경매장</yellow>" }
  type: command
  command: "경매장"        # '/' 없이
  # as: console            # 콘솔 실행으로 바꾸면 {player} 치환 사용 가능

# ③ 메시지(+클릭 링크)
my-wiki:
  slot: [40]
  item: { material: PAPER, name: "<white>공식 위키</white>" }
  type: message
  message:
    text: "<green><u>공식 위키 바로가기</u></green>"
    url: "https://wiki.scfarm.kr"     # 있으면 채팅 메시지 클릭 시 링크 열림
    hover: "<gray>클릭하여 열기</gray>"

# ④ 다른 메뉴 열기
to-jobs:
  slot: [22]
  item: { material: PAPER, name: "<white>직업</white>" }
  type: open
  open: jobmenu             # menus/jobmenu.yml

# ⑤ 닫기
exit:
  slot: [44]
  item: { material: BARRIER, name: "<red>닫기</red>" }
  type: close
```

### 조건부 버튼 (직업 전용 등)
```yaml
mine:
  slot: "20-21, 29-30"
  view-requirement:
    permission: "menu.job.miner"
    on-fail: locked                     # hide = 아예 숨김 / locked = 잠금 아이템 표시
    locked-item: { material: PAPER, custom-model-data: 20099, name: "<red>잠김</red>" }
  click-requirement:
    permission: "menu.job.miner"
    deny-message: "<red>사용 권한이 없습니다.</red>"
  item: { material: PAPER, name: "<gray>광산</gray>" }
  type: warp
  warp: mine
```

### 클릭 동작 규칙 (중요)
- **모든 버튼은 클릭 즉시 GUI가 닫히고**, 동작(명령/이동/메시지)은 바로 다음 틱에 실행됩니다.
- 더블클릭해도 동작은 1번만 실행됩니다.

---

## 5. 기능 테스트 체크리스트

> 테스트 서버(플레이어 1~2명)에서 아래 순서대로. ✅ 표시하며 진행하세요.

### A. 설치/로드
- [ ] 서버 시작 → 콘솔에 `MenuPlugin - 연결 성공` (주황) 출력
- [ ] `plugins/MenuPlugin/config.yml`, `menus/hub.yml` 생성됨
- [ ] 에러/경고 스택트레이스 없음

### B. 메뉴 열기
- [ ] `/메뉴` → 5줄(45칸) GUI 열림, 타이틀 정상 (Nexo 있으면 글리프, 없으면 빈 타이틀)
- [ ] `/menu`, `/apsb` 도 동일하게 열림
- [ ] **Shift+F** → 열림 / **스니크 없이 F** → 메뉴 안 열리고 손 교체만 됨
- [ ] 열자마자 다시 `/메뉴` → 쿨다운 메시지 `메뉴는 3초에 한 번만 열 수 있습니다.`
- [ ] 3초 후 다시 열림

### C. 표시 확인
- [ ] 스폰(0-2,9-11) / 내 섬(3-4,12-13) / 상점(5-6,14-15) / 경매장(7-8,16-17) — 각 6칸·4칸에 같은 아이템
- [ ] 하단 36~44에 우편함/개인설정/미니게임/크레딧상점/위키/고객센터/디스코드/휴지통/창고
- [ ] 이름·로어 색상 정상, 이탤릭 자동 제거됨
- [ ] 중간 18~35는 빈칸

### D. 클릭 방어 (탈취 방지)
- [ ] 버튼 아이템을 클릭해도 **가져올 수 없음**
- [ ] Shift-클릭, 드래그, 숫자키(핫바 스왑)로도 못 가져옴
- [ ] 자기 인벤토리 클릭은 이벤트만 취소되고 아무 일 없음
- [ ] GUI 닫은 뒤 인벤토리에 버튼 아이템이 남아있지 않음 (유령 아이템 없음)

### E. 버튼 타입별 동작
- [ ] **warp**: 스폰 클릭 → GUI 즉시 닫힘 → 이동됨 (콘솔에 `warpother <닉> spawn` 실행 흔적)
- [ ] **warp (WarpCraft 미설치 서버)**: 클릭 → `지금은 이동 시스템을 사용할 수 없습니다.`
- [ ] **command**: 경매장 클릭 → GUI 닫힘 → `/경매장` 이 플레이어 권한으로 실행됨
- [ ] **message**: 위키 클릭 → GUI 닫힘 → 채팅에 밑줄 문구 출력, 호버 문구 표시, 클릭 시 URL 열림
- [ ] **더블클릭**: 아주 빠르게 2번 클릭해도 명령이 1번만 실행됨
- [ ] 클릭 사운드 재생됨 (spawn/island 버튼)

### F. 리로드
- [ ] hub.yml에서 스폰 버튼 name 수정 → `/메뉴리로드` → `메뉴 설정을 다시 불러왔습니다.` → 다시 열면 반영됨
- [ ] 리로드 순간 메뉴를 열고 있던 플레이어는 GUI가 자동으로 닫힘
- [ ] `open-commands`에 `"테스트메뉴"` 추가 → 리로드 → `/테스트메뉴` 즉시 동작 + 탭완성 표시
- [ ] **비OP**로 `/메뉴리로드` → `알 수 없는 명령어입니다.` / 탭완성에 명령어 자체가 안 보임
- [ ] LuckPerms로 `menu.reload` 권한을 준 비OP → 리로드 가능

### G. 조건부 버튼 (§4의 mine 예시를 hub.yml에 추가 후)
- [ ] 권한 없는 플레이어: `on-fail: hide` → 버튼 안 보임 / `locked` → 잠금 아이템 표시 + 클릭해도 무동작
- [ ] `click-requirement`만 있는 경우: 보이지만 클릭 시 `deny-message` 출력
- [ ] LuckPerms로 권한 부여 후 재오픈 → 정상 표시·동작

### H. 새 메뉴 + open 타입
- [ ] `menus/test.yml` 생성(hub 복사 후 타이틀 변경) → hub에 `type: open, open: test` 버튼 추가 → 리로드
- [ ] 클릭 → hub 닫히고 test 메뉴 열림 (쿨다운에 안 걸림)

### I. 필러
- [ ] hub.yml `filler.enabled: true` → 리로드 → 빈칸이 필러 아이템으로 채워짐, 클릭해도 무동작

### J. 예외 내성
- [ ] 버튼 name에 일부러 깨진 태그(`<green>닫는태그없음`) 입력 → 리로드 → 메뉴가 **열리기는 함** (해당 텍스트만 평문/이상 표시)
- [ ] 존재하지 않는 material 입력 → PAPER로 대체되어 표시
- [ ] slot에 `"999"` 입력 → 무시되고 나머지 정상

### K. 부하 (선택, 400명 대비)
- [ ] 봇/테스터 다수로 동시에 `/메뉴` 연타 → `/tps` 20 유지, `/timings`(또는 spark)에서 MenuPlugin 항목이 상위에 없음
- [ ] 수십 명 동시 warp 클릭 → 지연은 WarpCraft 처리량에 좌우됨(MenuPlugin은 위임만)

---

## 6. 문제 해결 (Troubleshooting)

| 증상 | 원인/조치 |
|---|---|
| `/메뉴` 가 "알 수 없는 명령어" | 플러그인 로드 실패(콘솔 확인) 또는 open-commands 오타. 로드 성공인데도 안 되면 `/메뉴리로드` |
| 글리프가 네모(□)로 표시 | Nexo 미설치이거나 리소스팩 미적용. Nexo의 glyph id(`menu/wiki/...`)가 실제 등록명과 일치하는지 확인 |
| warp 클릭해도 이동 안 됨 | ① WarpCraft 설치·활성 확인 ② 콘솔에서 직접 `warpother <닉> spawn` 실행해 WarpCraft 자체 동작 확인 ③ warp 이름이 WarpCraft DB 등록명과 일치하는지 확인 |
| 타이틀이 이상한 문자로 보임 | `<shift:-8>` 픽셀 시프트는 Nexo 리소스팩 폰트 기준입니다. Nexo 없는 서버에선 태그가 무시됩니다 |
| 리로드해도 반영 안 됨 | yml 문법 오류 가능성 — 콘솔의 `메뉴 로드 실패: ...` 경고 확인. 들여쓰기는 스페이스만 사용 |
| 아이템 CMD가 안 보임 | 리소스팩의 custom-model-data 정의와 material 이 일치해야 함 (PAPER + 20000) |

---

## 7. 파일 구조 참고

```
plugins/MenuPlugin/
├── config.yml        # 쿨다운/warp연동/리로드/Shift+F/메시지
└── menus/
    ├── hub.yml       # 허브 메뉴 (기본 제공)
    └── *.yml         # 추가 메뉴 (자유롭게 생성)
```

소스 구조·설계 원칙은 `README.md` 참고.
