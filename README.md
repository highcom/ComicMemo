# ComicMemoリポジトリの概要
このリポジトリで管理されているコードはGoogle Playストアにて「[巻数メモ ～ Comic Memo ～](https://play.google.com/store/apps/details?id=com.highcom.comicmemo&hl=ja)」アプリとして公開されています。
## このアプリの機能
マンガなどを何巻まで買ったのか管理するためのアプリです。詳細な機能については、Google Playストアの方で確認してください。
## クラス構成について
以下に示すクラス図と主要なクラスについて簡単に説明します。
![クラス図_ComicMemo](https://user-images.githubusercontent.com/12059529/104094381-ee07d580-52d3-11eb-9ae6-e78858e9ee9e.png)
### ComicMemo
アプリを立ち上げた時に最初に表示されるアクティビティ。巻数メモの一覧と追加や編集、検索の機能の操作を管理する。
### SectionPagerAdapter
このクラスが持っている続刊と完結のFragmentを管理する。
### PlaceHolderFragment
続刊と完結タブの中身を構成するクラス。ListDataManagerクラスから取得したデータをListViewAdapterクラスを使ってリストで表示する。
### SimpleCallbackHelper
Adapterに対するMoveやSwipe操作の実装クラス。Swipeして表示されるボタンの定義をしている。
### ViewHolder
１つのリストを表示するためのデータクラス。ListDataManagerクラスから取得したデータが格納される。
### PageViewModel
データに更新があった場合にSectionPagerAdapterクラスに通知する。
### ListDataManager
SQLite3で管理されているデータにアクセスするためのORマッパクラス。
### ListViewAdapter
巻数メモがリスト表示される1つのカラムの画面構成とその操作について定義されているクラス。
### InputMemo
巻数メモを新規作成・編集を行う際のアクティビティ。
## バージョンについて
v0.0としてタグを付けていますので、過去のリリースバージョンのコードはタグを過去に辿ることで参照出来ます。
