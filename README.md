# GotoPositionMod

## 概要
`GotoPositionMod`は、Minecraftのクライアント側で動作するModです。このModを使用することで、プレイヤーは特定の座標に瞬時に移動することができます。これは、大規模なマップを探索する際や、建築作業を効率化するために非常に便利です。

## 機能
- 特定の座標への瞬時移動
- 座標の保存と呼び出し
- GUIを介した簡単な操作

## インストール方法
1. 最新の`Fabric`をインストールします。
2. fabric-api をインストールします。
3. `GotoPositionMod`の最新版をダウンロードします。
4. ダウンロードした`.jar`ファイルを、Minecraftの`mods`フォルダに移動します。
5. Minecraftを起動し、Modがリストに表示されていることを確認します。

## 使い方
Modがインストールされた状態でMinecraftを起動します。

### 座標指定モード開始
```
/gotoposition x z
```

### 座標指定モード解除
```
/gotoposition
```


### ユーザ指定モード開始
```
/gotouser username
```

### ユーザ指定モード解除
```
/gotouser
```


## 開発者向け情報
このModは、`GotoPositionModClient`クラスを中心に構築されています。このクラスは`ClientModInitializer`インターフェースを実装しており、Modの初期化処理を担当しています。

## ライセンス
このModは[CC 1.0 Universal](LICENSE)の下で公開されています。

## コントリビューション
このプロジェクトへの貢献を歓迎します。バグ報告、機能提案、プルリクエストなど、お気軽にご連絡ください