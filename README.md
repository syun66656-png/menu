# MenuPlugin (마인팜 메뉴 플러그인)

완전 **config 기반** 메뉴 플러그인. 버튼(슬롯)마다 `type` 으로 이동/명령어/메시지를
유동 지정 — 하드코딩 없음. 코드는 config 를 "실행"만 한다.

- 이동은 **WarpCraft** 에 위임(콘솔 `warpother {player} {warp}`) — 메뉴는 DB 를 만지지 않음.
- 모든 텍스트 **MiniMessage** + **Nexo** glyph/shift 태그 지원(Nexo 없으면 우아하게 저하).
- 패키지 `kr.scfarm.menu`, 결과물 `target/MenuPlugin.jar`.

## 빌드

```bash
mvn clean package
```

- `pom.xml` 의 `paper.version` / `maven.compiler.release` 로 대상 버전을 맞춘다.
  기본값은 설계서 기준 **Paper 26.2(실험) / Java 25**. 안정 빌드는 `26.1.2` 로 교체.
- 26.x 는 MojMap → **리매핑 불필요**, 순정 `paper-api` 로 컴파일(paperweight 불필요).
- Nexo 는 소프트 의존이며 런타임 리플렉션으로 연동하므로 컴파일에 강제되지 않는다.
  컴파일 타임 연동을 원하면 `pom.xml` 의 Nexo 의존 주석을 해제.
- `finalName` 고정으로 IntelliJ/Maven 빌드 시 `-spigot-1.0.0` 같은 접미사가 붙지 않는다.

## 파일 구성

```
config.yml            # 전역 (쿨다운/warp연동/리로드/메시지/Shift+F)
menus/
  hub.yml             # 허브 메뉴 정의
```

메뉴 추가는 `menus/` 에 `.yml` 파일을 추가하고, 버튼에서 `type: open` 으로 연다.
`/메뉴리로드`(OP 전용) 로 config + menus 를 다시 불러온다(명령어도 재등록).

## 버튼 타입(`type`)

| type | 동작 | 주요 필드 |
|---|---|---|
| `warp` | WarpCraft 이동 위임 | `warp` |
| `command` | 명령어 실행 | `command`, `as: player\|console` |
| `message` | 메시지(+선택 링크) | `message.{text,url,hover}` |
| `open` | 다른 메뉴 열기 | `open` |
| `close` | 닫기 | — |

슬롯은 단일(`36`) / 나열(`[3,4]`) / 범위 묶음(`"0-2, 9-11"`) 을 모두 허용한다.

## 확정 필요(설계서 §12)

config 로 전부 바꿀 수 있으므로 코드 수정 없이 아래를 조정하세요.

1. WarpCraft warp 실제 이름: `spawn / island / shop / minigame / creditshop`
2. 중간 슬롯(18~35) 직업 버튼 추가 여부
3. 40/41/42 메시지·URL(위키/고객센터/디스코드) 실제 값
4. 명령어명: `경매장/우편함/개인설정/쓰레기통/창고`
5. 타겟 버전: 26.2(실험) vs 26.1.2(안정) → `pom.xml`

> **빌드 참고:** 이 저장소가 만들어진 원격 환경에서는 조직 egress 정책이
> `repo.papermc.io` 를 차단(403)하여 CI 컴파일을 실행하지 못했다. 실제 Paper
> 저장소에 접근 가능한 환경에서 `mvn package` 로 빌드하세요.
