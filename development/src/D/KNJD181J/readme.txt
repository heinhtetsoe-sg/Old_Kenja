// kanji=漢字
// $Id: readme.txt 73180 2020-03-23 04:08:37Z maeshiro $

2014/07/01  1.KNJD171Jをもとに作成

2014/07/09  1.プロパティー整理（reportSpecialSize01_02、reportSpecialSize02_01、reportSpecialSize04_01、tutisyoSougouHyoukaNentani追加、HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_J、HREPORTREMARK_DAT_COMMUNICATION_SIZE_J、reportSpecialSize03_02削除）

2014/07/14  1.プロパティーtutisyoSougouHyoukaTunen修正

2014/08/11  1.style指定修正

2015/06/02  1.異動対象日付の変更処理を追加

2016/01/21  1.SEMESTER_GRADE_MST追加に伴う修正

2016/08/23  1.宮城県以外は「押印欄を出力する」チェックボックス追加

2018/07/05  1.プロパティ「reportSpecialSize04_01Title」を読み込んでJavaに引き渡す変数に設定する処理を追加
            2.京都のみ「教育目標印字なし」チェックボックス追加
            3.「教育目標印字なし」チェックボックスのデフォルト値を未チェックに変更

2018/12/18  1.パラメータuseFormNameD181J_3、documentMstSize_B4追加

2019/07/16  1.3学期制の場合は「所見を出力する」チェックボックス追加
            2.「所見を出力する」を「所見を表示しない」に変更

2019/12/24  1.帳票側にtutisyoShokenntunenプロパティを引き継ぐよう、変更
              -- tutisyoShokenntunenプロパティ追加

2020/03/23  1.プロパティーtutisyoShokennSemesterTitle追加

2021/02/15  1.リファクタリング
            2.Pageの出力順で修了証を一番先に出力する
