# kanji=漢字
# $Id: readme.txt 56585 2017-10-22 12:47:53Z maeshiro $

2005.06.08 nakamoto 新規作成[緊急連絡網ＣＳＶ出力]
2005.07.07 nakamoto ＣＳＶで書き出す時にクラスはクラス略称を出力
2005.07.27 nakamoto ファイル名のクラスをカット
2005.10.12 nakamoto 「現住所電話番号」をschreg_base_mstの急用電話番号(EMERGENCYTELNO)から出力するように変更

2010/04/06  1.tokio:/usr/local/development/src/A/KNJA320からコピーした。
            2.「生徒電話番号」「緊急連絡先 」ラジオボタンを追加
            --「生徒電話番号」は「SCHREG_ADDRESS_DAT」から
            --「緊急連絡先」は「SCHREG_BASE_MST」から

2010/04/08  1.中学なのか高校なのかの判別のSQLに年度の条件を追加

2016/11/14  1.年組取得のcommon.php参照をカット
            2.余分な空白をカット

2017/04/25  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind
