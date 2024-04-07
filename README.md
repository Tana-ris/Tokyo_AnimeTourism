## 聖地巡礼アプリ『SEICHI』の概要

立正大学後藤研究室として，[都知事杯オープンデータ・ハッカソン](https://odhackathon.metro.tokyo.lg.jp/)（2023年）に参戦。  
アニメの聖地巡礼に着目し，外国人観光客向けに観光ルートを提案するAndroidアプリケーション ***『SEICHI』*** を提案。  
今回は新海誠監督が手掛けた『[天気の子](https://tenkinoko.com/)』を選択した。

<br />

ルート提案は以下の流れで行っている。  
1. 　現在地取得  
2. 　最寄りの[自転車シェアリング場](https://www.odpt.org/2022/06/28/press20220628_bikeshare/)を検索
3. 　[行きたい聖地](https://shinkaifan.com/pilgrimage-map/)（目的地）を選択  
4. 　最終目的地に近い[HOTEL・旅館](https://catalog.data.metro.tokyo.lg.jp/dataset/t000029d0000000003)を検索  
5. 　GoogleのDirections APIを用いて，「自転車シェアリング場➡聖地1➡聖地2➡聖地3➡HOTEL・旅館」といったルートを検索  
7. 　マップ上に表示し，掛かる時間を表示する

<br />

外国人向けのアプリであるため表記は英語のみ。  
このアプリは聖地巡礼がメインだが，[東京都の美術館](https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-P27.html)のルート提案，緊急時における[最寄りの避難所](https://catalog.data.metro.tokyo.lg.jp/dataset/t131041d0000000055/resource/4cb825de-9658-45e6-83d9-a08f2228c4a4)へのルート提案も行えるように構築している。

<br />

## DEMO動画（画像をクリックするとYouTubeに飛びます）

[![DEMO動画](https://github.com/Tana-ris/Tokyo_AnimeTourism/blob/main/SEICHI.png)](https://youtu.be/i-fXMteILKQ)

<br />

## 成果発表時のスライド（一部抜粋）
| 現状 |　課題と解決案 |
| ---- | ---- |
| ![現状](https://github.com/Tana-ris/Tokyo_AnimeTourism/assets/142727754/5cd3cf37-6e98-415b-aca8-c9512764494a) | ![課題と解決案](https://github.com/Tana-ris/Tokyo_AnimeTourism/assets/142727754/f59aca12-eadf-455b-b4d3-f66290f14551) |
| 外国人旅行者の増加に比例して「アニメ聖地に訪問したい」というニーズも急激に高まっている！ | ストレスのない聖地巡礼をバックアップ！行きたい聖地を選択するとルートを提案！ |

| システム構築図 |　使い方 |
| ---- | ---- |
| ![フロー](https://github.com/Tana-ris/Tokyo_AnimeTourism/assets/142727754/5228dd23-204a-45e3-b309-e28efbf43247) | ![使い方](https://github.com/Tana-ris/Tokyo_AnimeTourism/assets/142727754/bf764e21-ece9-49db-9f6d-b3595fd072e8) |
| Platform：Android Studio | ① アプリを起動し，表示したい地図を選択！|
| API：Maps SDK for Android，Directions API | ② 行きたい聖地をタップ！|
| Data：Anime Data（シーン毎の緯度経度），東京都のOpen Data（自転車シェアリング，HOTEL・旅館，文化施設，避難所） | ③ ルートと所要時間が表示される！|

<br />


## Requirement

（編集中）

<br />

## 今後の展望
将来的にはアプリケーションの向上を目指し，以下のアップデートを行う定です。  
- ARを導入しアニメと同じ画角がわかるようにする
- 施設の利用状況や人気スポットなども追加
- 英語だけでなく，アジア圏の言語にも対応した表示を行う

<br />

## チームメンバー
![チーム紹介](https://github.com/Tana-ris/Tokyo_AnimeTourism/assets/142727754/628507ca-d5b6-427f-bbba-ea567011a00f)
* Tana-ris  
  立正大学大学院　地球環境科学研究科　環境システム学専攻　修士2年  
          深層学習を用いた土砂災害の被災地早期検知について研究している
    
* 後藤真太郎先生
  立正大学　地球環境科学部　環境システム学科　教授
  今回のハッカソンの発起人
  
* kyonsato  
  立正大学大学院　地球環境科学研究科　環境システム学専攻　博士3年
  ドローンを用いた水稲の収量予測を専門に幅広く活動中
  
* shima  
  立正大学　地球環境科学部　環境システム学科　4年
  シミュレーションを用いて令和元年東日本台風時の洪水氾濫について研究している

<br />

