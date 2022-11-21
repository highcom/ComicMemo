# ComicMemoリポジトリの概要
このリポジトリで管理されているコードはGoogle Playストアにて「[書籍管理ができる巻数メモ ～持ってる書籍をメモして管理～](https://play.google.com/store/apps/details?id=com.highcom.comicmemo&hl=ja)」アプリとして公開されています。

## このアプリの機能
マンガなどを何巻まで買ったのか管理するためのアプリです。  
詳細な機能については、上記のGoogle Playストアの方で確認できます。

## クラス構成について
v2.0系からv3.0系にアップデートするにあたり、画面レイアウト・内部のアーキテクチャ構成についてリファクタリングしています。  
また、使用言語もJavaからKotlinに変更しています。  
以下からはリファクタリング前後でのクラス構成について説明します。  

## v3.0系
以下に示すクラス図と主要なクラスについて簡単に説明します。  
![クラス図_ComicMemo_リファクタ](https://user-images.githubusercontent.com/12059529/202936891-f11154f8-c428-4f33-85db-5772530f4e99.png)
#### ComicMemo
アプリを立ち上げた時に最初に表示されるアクティビティ。巻数メモの一覧と追加や編集、検索の機能の操作を管理する。
#### ComicPagerViewModelFactory
巻数データ一覧の操作用ViewModel生成用のファクトリクラス
#### SectionPagerAdapter
このクラスが持っている続刊と完結のPlaceHolderFragmentのインスタンスを生成して管理する。
#### SimpleCallbackHelper
Adapterに対するMoveやSwipe操作の実装クラス。Swipeして表示されるボタンの定義をしている。
#### PlaceHolderFragment
続刊と完結タブの中身を構成するクラス。ComicPagerViewModelのクラスでのデータ更新を監視してリストを更新する。
#### InputMemo
巻数メモを新規作成・編集を行う際のアクティビティ。
#### ComicListAdapter
巻数メモがリスト表示される1つのカラムの操作について定義されているクラス。
#### ComicViewHolder
ComicListAdapterのinnerクラス。１つのカラムの表示構成について定義したクラス。Adapterクラスからデータがバインドされる。
#### ComicPageViewModel
巻数データ一覧を操作するためのViewModelクラス。続刊・完結のデータ更新を監視できるLiveDataとデータを更新するためのAPIを提供する。
#### ComicMemoApplication
自身のアプリケーションコンテキストを注入してRepositoryとRoomDatabaseをインスタンス化するクラス。
#### Comic
巻数データのエンティティクラス。
#### ComicDao
巻数データへのデータアクセスオブジェクトクラス。
#### ComicMemoRepository
リポジトリパターンによりデータ操作を抽象化したAPIを提供するクラス。
#### ComicMemoRoomDatabase
SQLite3で管理されているDBデータにアクセスするためのRoomライブラリを利用したDB操作クラス。V2.0系からのマイグレーションも行う。

## v2.0系
以下に示すクラス図と主要なクラスについて簡単に説明します。  
v2.5タグがv2.0系の最新となります。  
![クラス図_ComicMemo](https://user-images.githubusercontent.com/12059529/104094381-ee07d580-52d3-11eb-9ae6-e78858e9ee9e.png)
#### ComicMemo
アプリを立ち上げた時に最初に表示されるアクティビティ。巻数メモの一覧と追加や編集、検索の機能の操作を管理する。
#### SectionPagerAdapter
このクラスが持っている続刊と完結のFragmentを管理する。
#### PlaceHolderFragment
続刊と完結タブの中身を構成するクラス。ListDataManagerクラスから取得したデータをListViewAdapterクラスを使ってリストで表示する。
#### SimpleCallbackHelper
Adapterに対するMoveやSwipe操作の実装クラス。Swipeして表示されるボタンの定義をしている。
#### ViewHolder
１つのリストを表示するためのデータクラス。ListDataManagerクラスから取得したデータが格納される。
#### PageViewModel
データに更新があった場合にSectionPagerAdapterクラスに通知する。
#### ListDataManager
SQLite3で管理されているデータにアクセスするためのORマッパクラス。
#### ListViewAdapter
巻数メモがリスト表示される1つのカラムの画面構成とその操作について定義されているクラス。
#### InputMemo
巻数メモを新規作成・編集を行う際のアクティビティ。
## バージョンについて
vX.Xとしてタグを付けていますので、過去のリリースバージョンのコードはタグを過去に辿ることで参照出来ます。
## ライセンス
MIT
