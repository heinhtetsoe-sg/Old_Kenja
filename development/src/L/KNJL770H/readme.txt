// kanji=漢字
// $Id: 38da4734439b4110143f1bbab4cc4b29583dac11 $

2020/10/05  1.新規作成

2020/10/06  1.以下の修正をした
            --取消機能追加
            --繰上合格点に合格点以上の値が入力されたときのエラーメッセージ文言を修正

2020/11/17  1.デバッグ処理をカット
            2.確定情報削除処理を追加
            3.確定ボタン押下時、指定した入試区分・性別の確定情報が登録済みの場合にエラーメッセージを表示するよう修正

2020/11/26  1.確定情報を指定した性別毎に削除するよう修正

2021/04/13  1.性別「共通」でシミュレーションを行った場合、「男性」の合格者数が表示される不具合修正
            2.リファクタリング
            3.指示画面の条件を変更した際に、シミュレーション結果をリセットするよう修正
            4.入力した合格点・繰上合格点は条件を変更しても保持されるよう修正
            5.コード自動整形
            6.合計フィールド変更に伴い修正
            --変更前：ENTEXAM_RECEPT_DAT.TOTAL4
            --変更後：ENTEXAM_RECEPT_DETAIL_DAT.SEQ=009のREMARK6
