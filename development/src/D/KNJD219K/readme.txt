# kanji=漢字
# $Id: f0ecba6af6dc2dfc6506e92d5d66efca571dae90 $

2019/05/21  1.KNJD219Bを参考に新規作成

2019/06/10  1.校種を制御する処理追加

2019/07/12  1.プロパティーknjd219kUseAssessCourseMstが1なら算出した値をASSESS_COURSE_MST(ASSESSCD='2')で換算した値で更新する
            2.プロパティーknjd219kUseAssessCourseMstが1なら算出した値をASSESS_COURSE_MST(ASSESSCDは学年末評定は3、それ以外は2)で換算した値で更新する

2019/09/03  1.プロパティーが設定されていたら、SDIV=01のテストを纏めて割合設定する。
            -- KNJD219J_SeisekiSanshutsuPattern
            -- 学年末成績(9-9900-08)にもセットする

2019/09/03  1.プロパティーKNJD219J_SeisekiSanshutsuPatternが1の時、SDIV=01のテストを纏めて割合設定する。
            -- その割合設定で算出する。また、学年末成績(9-9900-08)にもセットする

2020/03/17  1.プロパティーknjd219kUseAssessCourseMstが2の時、学年末成績(9-9900-08)は評定マスタで換算しない値で更新する

2020/10/20  1.プロパティーKNJD219K_OtherScorePatternが1の時、学期成績自動算出時に見込み点を利用するよう、変更

2020/11/02  1.評定マスタの参照条件を変更。
