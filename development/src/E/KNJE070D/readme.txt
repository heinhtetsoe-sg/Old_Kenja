# kanji=漢字
# $Id: 069a7800dd388c8a872107a85a438df6930db2cf $

2020/05/21  1.KNJE070をコピーして作成

2020/05/22  1.とりあえずCSVボタンをカット

2020/06/19  1.プロパティKNJXEXP_SEARCHがSCHREGNO_NORMALの場合、生徒指定順を学籍番号順にする

2020/07/15  1.プロパティーtyousasyoHankiNintei使用追加

2020/07/27  1.フォームが3年用の場合「6年用フォーム」、フォームが4年用の場合「8年用フォーム」のチェックボックスを表示する
        
2020/11/11  1.プロパティーtyousashoPrintStampSelectが1の場合、校長印出力・担任印出力指定チェックボックスを追加する
              プロパティーtyousashoPrintStampSelectが2の場合、校長印出力のみ指定チェックボックスを追加する

2020/11/19  1.文言担任印出力を記載責任者印出力に変更

2020/11/24  1.コード自動整形
            2.プロパティーtyousasho2020PrintHeaderNameがcheckの場合、偶数頁の氏名出力チェックボックスを追加する

2020/11/27  1.プロパティーtyousasho2020GvalCalcCheckが1の場合、評定平均の算出指定チェックボックスを追加する

2020/12/15  1.プロパティーtyousasyo_shokenTable_Seqが1の場合、所見パターン選択ラジオボタンを表示する

2020/12/23  1.選択学籍番号がサブミット時に選択されない不具合を修正 （11/24 1.修正のバグ）

2021/01/29  1.プロパティhyoteiYomikaeRadioが1の場合、評定読替のチェックボックスを「評定読替・切替しない」「評定１を２に読替」「評定１を非表示」のラジオボタンに変更する
            2.プロパティhyoteiYomikaeRadioが1の場合の評定読替デフォルトを変更

2021/02/22  1.プロパティtyousasho2020PrintAvgRank=1の場合、評定平均席次出力する/しないラジオボタンを表示する
            --tyousasho2020GvalCalcCheckが1かつ多重平均選択時のみ活性化
