$Id: readme.txt 64740 2019-01-18 04:39:55Z matsushima $

2016/09/29  1.KNJA130C(1.36)を元に新規作成

2017/02/21  1.身分証番号6桁表示から、8桁全て表示に変更。

2017/02/22  1.身分証発行、食券機連携コンボ追加
            --CSV出力追加（食券機連携）
            2.食券機連携CSV出力変更
            --GRD_DIVがnullの場合、在籍は「0」
            --SUBIDは在籍データの学年

2017/03/06  1.身分証発行磁気なしの指定、出力追加

2017/03/09  1.初期値をクラスに変更
            2.クラス指定時の学年コンボをカットしてソート順を学科、年組に変更

2017/03/27  1.食券機指定の際のDBエラー修正 (前回の修正のバグ対応)

2017/04/27  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2018/04/04  1.以下の表示順を「学科、年組」から「年組」に変更
            --クラス指定時のリストtoリスト
            --個人指定時の年組コンボ

2018/04/06  1.食券機連携CSV出力変更
            -- 同じ郵便番号で複数件データが取得されたので、
            -- 郵便番号での都道府県取得は郵便NOが大きいほうに変更

2019/01/18  1.CSV出力の文字化け修正(Edge対応)