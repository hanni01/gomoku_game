## 📌 프로젝트 개요

이 프로젝트는 리눅스 기반 임베디드 보드에서 **키패드 디바이스 드라이버**를 통해  
입력 이벤트(KEY_2/4/5/6/8 등)를 발생시키고, 게임 진행 내용을 **Text LCD**를 통해 나타낸다.

Android Studio에서 **JNI(C++)로 해당 입력을 수신하여 오목(Gomoku) 게임**을 구현한 프로젝트.

임베디드 ↔ 안드로이드 **하드웨어-소프트웨어 통합 예제**

## 📌 구현 결과 영상 ↓↓

<a href="https://youtube.com/shorts/0MwDghmP1BQ?feature=share">
  <img src="https://github.com/user-attachments/assets/d77312ed-8f53-421a-a79d-e70e69cc2fc5" alt="구현 결과 영상 참고" width="400">
</a>

---

## 🧩 주요 구성

- 🔧 **키패드 디바이스 드라이버 (C)**  
  - `input subsystem` 기반 드라이버로 `/dev/input/event5`에 키 이벤트 전송  
  - KEY_2/4/6/8 → 방향 이동, KEY_5 → 착수
 
- 🔧 **TEXT LCD 디바이스 드라이버 (C)**
  - /dev/fpga_textlcd를 O_WRONLY로 open
  - 받은 text를 s4210 보드의 LCD에 뜨도록 함

- 📡 **Android JNI 연동 (NDK + C++)**  
  - `/dev/input/event5`를 open하여 키 수신  
  - `onKeyInput()`을 통해 Java와 실시간 통신
  - `sendTurnToBoard(String text)`을 통해 텍스트 수신

- 🎮 **오목 게임 (Java)**  
  - 14x15 격자 오목판, 돌 커서 이동 및 착수 구현  
  - 격자 계산, 커스텀 뷰(GridView), 좌표 기반 돌 렌더링 포함
  - 착수 기준 5방향(상하좌우, 대각선)에서 5오목이 됐는지 확인하는 알고리즘
  - 턴 전환 등
---

## 💡 특징

- 임베디드 디바이스 드라이버 + 사용자 앱(JNI) + UI 연동 예제
- Android NDK, 이벤트 처리, 이미지 뷰 위치 연산 등
- 오목판 격자, 돌 위치, 승리 판정 등 게임 로직 확장 가능

---

## 🛠️ 개발 환경

- Android Studio (Java)
- Android NDK (C/C++, JNI)
- Embedded 보드 (e.g. HBE-SM5-S4210-M3 등)
