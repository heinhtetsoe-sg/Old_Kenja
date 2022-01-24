<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knja228aModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;       //コントロールマスタ
    public $staffcd;       //職員コード
    public $select_opt  = array(); //セレクトデータ
    public $selectleft;
    public $schoolName;

    /**
     * メンバ変数の初期化
     */
    public function knja228aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    public function init()
    {
        $this->field = array("YEAR"          => VARS::post("YEAR"),          //年度
                            "GAKKI"          => VARS::post("GAKKI"),         //学期コード
                            "KUBUN"          => VARS::post("KUBUN"),         //クラス・個人表示指定
                            "GRADE_HR_CLASS" => VARS::post("GRADE_HR_CLASS"),//クラス
                            "CLASS_SELECTED" => VARS::post("CLASS_SELECTED"),//クラス
                            "DBNAME"         => VARS::post("DBNAME")         //DB変更に備えて
        );

        $this->cmd = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        if ($this->cmd != 'change_class') {
            $this->select_opt = array();
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }

    /**
     * 引数のSQLでDBからデータ取得
     */
    private function getDataWithSql($db, $sql)
    {
        $ret = array();

        $result = $db->query($sql);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $ret[]= $row;
        }
        $result->free();

        return $ret;
    }

    /**
     * 「/」または「-」区切りの日付文字列の0埋めを削除して「/」で区切り直す
     */
    private function formatDate($dt)
    {
        $dt = str_replace('/', "\b", $dt);
        $dt = str_replace('-', "\b", $dt);

        $dt_ = explode("\b", $dt);

        return (int)$dt_[0] .'/'. (int)$dt_[1] .'/'. (int)$dt_[2];
    }

    /**
     * 1人分の生徒のCSV文字列を取得
     */
    private function getCsvContent($recRegNo)
    {
        $DUMMY = "\b";//通常使用しない文字ならなんでも良い
        $INIT_ROW = 80;
        $INIT_COL = 22;
        $CSV_EL = "\r\n";//CSVとしての改行コード

        //DB接続
        $db = Query::dbCheckOut();

        //データ配列の枠だけ作成する。ブランクと区別するためダミーの文字列で埋める
        $contentData = array();
        for ($i = 0; $i < $INIT_ROW; $i++) {
            for ($j = 0; $j < $INIT_COL; $j++) {
                $contentData[$i][$j] = $DUMMY;
            }
        }

        //複数箇所で参照するため事前に宣言
        $gradeInfo = array();//学年情報
        $currentClass = '';  //現在の学年
        $studentName = '';   //生徒名


        //データ取得＆セット(年、組、番、担任)
        //CSVデータに固定値をセット
        $contentData[0][ 7] = "年度";
        $contentData[0][ 8] = "学年";
        $contentData[0][ 9] = "組";
        $contentData[0][10] = "番号";
        $contentData[0][11] = "学科";
        $contentData[0][12] = "担任";

        //DBからデータ取得
        $gradeInfo = $this->getDataWithSql($db, knja228aQuery::getCsvBasicData($this, $recRegNo));

        //現在の学年を保持
        foreach ($gradeInfo as $val) {
            if ($val['YEAR'] == CTRL_YEAR) {
                $currentClass = "{$val['GRADE_NAME']} {$val['HR_CLASS_NAME2']}組 {$val['ATTENDNO']}番";
            }
        }

        //CSVデータにセット
        $OFFSET_GRADE_ROW = 1;
        $OFFSET_GRADE_COL = 7;
        $i = 0;
        $prevNendo = "9999";
        foreach ($gradeInfo as $rowKey => $rowVal) {
            if ($prevNendo != $rowVal['YEAR']) {
                $prevNendo = $rowVal['YEAR'];
            } else {
                //印字位置を戻して印字(担任を上書きする)
                $i--;
            }
            $contentData[$OFFSET_GRADE_ROW + $i][$OFFSET_GRADE_COL + 0] = common::DateConv1($rowVal['YEAR']."/04/01", 2).'度';//年度
            $contentData[$OFFSET_GRADE_ROW + $i][$OFFSET_GRADE_COL + 1] = $rowVal['GRADE_NAME'];//学年
            $contentData[$OFFSET_GRADE_ROW + $i][$OFFSET_GRADE_COL + 2] = $rowVal['HR_CLASS_NAME2'];//組
            $contentData[$OFFSET_GRADE_ROW + $i][$OFFSET_GRADE_COL + 3] = $rowVal['ATTENDNO'];//番号
            $contentData[$OFFSET_GRADE_ROW + $i][$OFFSET_GRADE_COL + 4] = $rowVal['MAJORNAME'];//学科
            $contentData[$OFFSET_GRADE_ROW + $i][$OFFSET_GRADE_COL + 5] = $rowVal['STAFFNAME'];//担任
            $i++;
        }


        //同じ年度ものものは1行にまとめる
        $tempGradeYearKey = "9999";
        $tempGradeInfo = array();
        foreach ($gradeInfo as $rowKey => $rowVal) {
            if ($tempGradeYearKey != $rowVal['YEAR']) {
                $tempGradeInfo[$rowKey] = $rowVal;
                $tempGradeYearKey = $rowVal['YEAR'];
            }
        }
        $gradeInfo = $tempGradeInfo;

        //データ取得＆セット(生徒 基本情報)
        $contentData[8][0] = "生徒  基本情報";

        //生徒 基本情報
        //CSVデータに固定値をセット
        $contentData[ 9][0] = "氏名かな";
        $contentData[10][0] = "氏名";
        $contentData[11][0] = "学籍番号";
        $contentData[11][4] = "性別";
        $contentData[11][6] = "生年月日";
        $contentData[12][0] = "住所";
        $contentData[13][0] = "住所区分";
        $contentData[13][4] = "居住地域";
        $contentData[14][0] = "電話番号";
        $contentData[14][4] = "FAX番号";
        $contentData[15][0] = "携帯電話";
        $contentData[15][4] = "メールアドレス";
        $contentData[16][0] = "通学時間";
        $contentData[16][4] = "通学方法";
        $contentData[17][0] = "出身校";
        $contentData[17][4] = "入学区分";
        $contentData[19][0] = "進路希望";
        $contentData[19][1] = "進学";
        $contentData[20][1] = "就職";

        //DBからデータ取得
        $studentBasicData = $this->getDataWithSql($db, knja228aQuery::getCsvStudentBasicData($this, $recRegNo));

        //CSVデータにセット
        $contentData[ 9][1] = $studentBasicData[0]['NAME_KANA'];
        $contentData[10][1] = $studentBasicData[0]['NAME'];
        $contentData[11][1] = $studentBasicData[0]['SCHREGNO'];
        $contentData[11][5] = $studentBasicData[0]['SEX_NAME'];
        $contentData[11][7] = $this->formatDate($studentBasicData[0]['BIRTHDAY']);
        $contentData[12][1] = $studentBasicData[0]['ADDR1']."\n".$studentBasicData['ADDR2'];
        $contentData[13][1] = $studentBasicData[0]['RESIDENT_NAME'];
        $contentData[13][5] = $studentBasicData[0]['AREA_NAME'];
        $contentData[14][1] = $studentBasicData[0]['TELNO'];
        $contentData[14][5] = $studentBasicData[0]['FAXNO'];
        $contentData[15][1] = $studentBasicData[0]['TELNO2'];
        $contentData[15][5] = $studentBasicData[0]['EMAIL'];
        $contentData[16][1] = $studentBasicData[0]['COMMUTE_TIME'];
        $contentData[16][5] = $studentBasicData[0]['HOWTOCOMMUTE_NAME'];
        $contentData[17][1] = $studentBasicData[0]['FINSCHOOL_NAME'];
        $contentData[17][5] = $studentBasicData[0]['ENT_DIV_NAME'];
        $contentData[19][4] = $studentBasicData[0]['PLANUNIV'];
        $contentData[20][4] = $studentBasicData[0]['PLANJOB'];

        //生徒名を保持
        $studentName = $studentBasicData[0]['NAME'];

        //留学情報取得
        //CSVデータに固定値をセット
        $contentData[18][0] = "留学";

        //DBからデータ取得
        $transferData = $this->getDataWithSql($db, knja228aQuery::getCsvTransferData($this, $recRegNo));
        foreach ($transferData as $key => $val) {
            //日付の書式を調整
            $transferData[$key]['TRANSFER_SDATE'] = $this->formatDate($transferData[$key]['TRANSFER_SDATE']);
            $transferData[$key]['TRANSFER_EDATE'] = $this->formatDate($transferData[$key]['TRANSFER_EDATE']);
        }

        //CSVデータにセット
        $transferDataText = '';
        if (get_count($transferData) >= 2) {
            //留学のデータが2つ以上あった時
            $transferDataText .= $transferData[1]['TRANSFER_SDATE']."～".$transferData[1]['TRANSFER_EDATE']."　".$transferData[1]['TRANSFERPLACE'];
            $transferDataText .= "\n";
        }
        if (get_count($transferData) != 0) {
            $transferDataText .= $transferData[0]['TRANSFER_SDATE']."～".$transferData[0]['TRANSFER_EDATE']."　".$transferData[0]['TRANSFERPLACE'];
        }
        $contentData[18][1] = $transferDataText;

        //特別活動の記録

        //CSVデータに固定値をセット
        $contentData[22][0] = "特別活動の記録";
        $contentData[23][0] = "年度";
        $contentData[23][1] = "学年";
        $contentData[23][2] = "部活動";
        $contentData[23][5] = "委員会・生徒会";

        //DBからデータ取得
        $clubData      = $this->getDataWithSql($db, knja228aQuery::getCsvClubData($this, $recRegNo));
        $committeeData = $this->getDataWithSql($db, knja228aQuery::getCsvCommitteeData($this, $recRegNo));

        //年度と学年をCSVデータにセット
        $i = 0;
        foreach ($gradeInfo as $gradeVal) {
            $contentData[24 + $i][0] = common::DateConv1($gradeVal['YEAR']."/4/01", 2).'度';//年度
                $contentData[24 + $i][1] = $gradeVal['GRADE_NAME'];                      //学年
                $contentData[24 + $i][2] = '';                                           //(部活動名称の初期値をブランクで初期化)
                $contentData[24 + $i][5] = '';                                           //(委員会・生徒会名称の初期値をブランクで初期化)

                //部活動情報
            foreach ($clubData as $clubVal) {
                if ($gradeVal['YEAR'] != $clubVal['YEAR']) {
                    continue;
                }//異なる年度はスキップ

                $clubText = '';

                if ($contentData[24 + $i][2] != '') {
                    //すでに値が入っている場合は、既存の値 + 改行をセット
                    $clubText .= $contentData[24 + $i][2] . "\n";
                }

                $clubText .= $clubVal['CLUBNAME'];

                if ($clubVal['EXECUTIVE_NAME'] != null) {
                    //部活動で役職についているとき
                    $clubText .=  ' '.$clubVal['EXECUTIVE_NAME'];
                }
                if ($clubVal['EDATE'] != null) {
                    //引退しているとき
                    $clubText .=  ' '.$this->formatDate($clubVal['EDATE']);
                }

                //CSVデータにセット
                $contentData[24 + $i][2] = $clubText;
            }

            //委員会・生徒会
            foreach ($committeeData as $committeeVal) {
                if ($gradeVal['YEAR'] != $committeeVal['YEAR']) {
                    continue;
                }//異なる年度はスキップ

                $committeeText = '';

                if ($contentData[24 + $i][5] != '') {
                    //すでに値が入っている場合は、既存の値 + 改行をセット
                    $committeeText .= $contentData[24 + $i][5]. "\n";
                }

                $committeeText .= $committeeVal['COMMITTEENAME'];

                if ($committeeVal['EXECUTIVE_NAME'] != null) {
                    //委員会・生徒会で役職についているとき
                    $committeeText .=  ' '.$clubVal['EXECUTIVE_NAME'];
                }

                //CSVデータにセット
                $contentData[24 + $i][5] = $committeeText;
            }

            $i++;
        }

        //データ取得＆セット(保護者情報)
        //CSVデータに固定値をセット
        $contentData[0][15] = "保護者　基本情報";

        //保護者1
        //CSVデータに固定値をセット
        $contentData[1][15] = "保護者1";
        $contentData[1][16] = "氏名かな";
        $contentData[1][20] = "続柄";
        $contentData[2][16] = "氏名";
        $contentData[3][16] = "住所";
        $contentData[4][16] = "電話";
        $contentData[4][20] = "メール";

        //DBからデータ取得
        $guardian1Data = $this->getDataWithSql($db, knja228aQuery::getCsvGuardian1Data($this, $recRegNo));

        //CSVデータにセット
        $contentData[1][17] = $guardian1Data[0]['GUARD_KANA'];
        $contentData[1][21] = $guardian1Data[0]['RELATION_NAME'];
        $contentData[2][17] = $guardian1Data[0]['GUARD_NAME'];
        $contentData[3][17] = $guardian1Data[0]['GUARD_ADDR1'];
        $contentData[3][18] = $guardian1Data[0]['GUARD_ADDR2'];
        $contentData[4][17] = $guardian1Data[0]['GUARD_TELNO'];
        $contentData[4][21] = $guardian1Data[0]['GUARD_E_MAIL'];

        //保護者2
        //CSVデータに固定値をセット
        $contentData[5][15] = "保護者2";
        $contentData[5][16] = "氏名かな";
        $contentData[5][20] = "続柄";
        $contentData[6][16] = "氏名";
        $contentData[7][16] = "住所";
        $contentData[8][16] = "電話";
        $contentData[8][20] = "メール";

        //DBからデータ取得
        $guardian2Data = $this->getDataWithSql($db, knja228aQuery::getCsvGuardian2Data($this, $recRegNo));

        //CSVデータにセット
        $contentData[5][17] = $guardian2Data[0]['GUARD_KANA'];
        $contentData[5][21] = $guardian2Data[0]['RELATION_NAME'];
        $contentData[6][17] = $guardian2Data[0]['GUARD_NAME'];
        $contentData[7][17] = $guardian2Data[0]['GUARD_ADDR1'];
        $contentData[7][18] = $guardian2Data[0]['GUARD_ADDR2'];
        $contentData[8][17] = $guardian2Data[0]['GUARD_TELNO'];
        $contentData[8][21] = $guardian2Data[0]['GUARD_E_MAIL'];

        //身元引受人
        //CSVデータに固定値をセット
        $contentData[ 9][15] = "身元引受人";
        $contentData[ 9][16] = "氏名かな";
        $contentData[ 9][20] = "続柄";
        $contentData[10][16] = "氏名";
        $contentData[11][16] = "住所";
        $contentData[12][16] = "電話";

        //DBからデータ取得
        $guaranterData = $this->getDataWithSql($db, knja228aQuery::getCsvGuaranterData($this, $recRegNo));

        //CSVデータにセット
        $contentData[ 9][17] = $guaranterData[0]['GUARANTOR_KANA'];
        $contentData[ 9][21] = $guaranterData[0]['RELATION_NAME'];
        $contentData[10][17] = $guaranterData[0]['GUARANTOR_NAME'];
        $contentData[11][17] = $guaranterData[0]['GUARANTOR_ADDR1'];
        $contentData[11][18] = $guaranterData[0]['GUARANTOR_ADDR2'];
        $contentData[12][17] = $guaranterData[0]['GUARANTOR_TELNO'];

        //その他
        //CSVデータに固定値をセット
        $contentData[13][15] = "その他";
        $contentData[13][16] = "氏名かな";
        $contentData[13][20] = "続柄";
        $contentData[14][16] = "氏名";
        $contentData[15][16] = "住所";
        $contentData[16][16] = "電話";

        //DBからデータ取得
        $sendData = $this->getDataWithSql($db, knja228aQuery::getCsvSendData($this, $recRegNo));

        //CSVデータにセット
        $contentData[13][17] = $sendData[0]['SEND_KANA'];
        $contentData[13][21] = $sendData[0]['RELATION_NAME'];
        $contentData[14][17] = $sendData[0]['SEND_NAME'];
        $contentData[15][17] = $sendData[0]['SEND_ADDR1'];
        $contentData[15][18] = $sendData[0]['SEND_ADDR2'];
        $contentData[16][17] = $sendData[0]['SEND_TELNO'];

        //緊急連絡先
        //CSVデータに固定値をセット
        $contentData[17][15] = "緊急連絡先";
        $contentData[17][16] = "緊急1";
        $contentData[17][17] = "氏名";
        $contentData[18][17] = "連絡先";
        $contentData[19][16] = "緊急2";
        $contentData[19][17] = "氏名";
        $contentData[20][17] = "連絡先";

        //DBからデータ取得
        $emergencyData = $this->getDataWithSql($db, knja228aQuery::getCsvEmergencyData($this, $recRegNo));

        //CSVデータにセット
        $contentData[17][18] = $emergencyData[0]['EMERGENCYNAME'];
        $contentData[18][18] = $emergencyData[0]['EMERGENCYTELNO'];
        $contentData[19][18] = $emergencyData[0]['EMERGENCYNAME2'];
        $contentData[20][18] = $emergencyData[0]['EMERGENCYTELNO2'];

        //兄弟姉妹
        //CSVデータに固定値をセット
        $contentData[21][15] = "兄弟\n姉妹";
        $contentData[21][17] = "兄弟姉妹1";
        $contentData[21][18] = "兄弟姉妹2";
        $contentData[21][19] = "兄弟姉妹3";
        $contentData[21][20] = "兄弟姉妹4";
        $contentData[22][16] = "名前";
        $contentData[23][16] = "続柄";
        $contentData[24][16] = "年組";

        //DBからデータ取得
        $siblingsData = $this->getDataWithSql($db, knja228aQuery::getCsvSiblingsData($this, $recRegNo));

        //CSVデータにセット
        $i = 0;
        foreach ($siblingsData as $val) {
            $contentData[22][17 + $i] = "{$val['RELAKANA']}\n{$val['RELANAME']}";
            $contentData[23][17 + $i] = $val['RELATION_NAME'];
            $contentData[24][17 + $i] = $val['GRADE_NAME'];

            $i++;
            if ($i == 4) {
                //4つで打ち止め
                break;
            }
        }

        //データ取得＆セット(成績)
        //CSVデータに固定値をセット
        $contentData[29][0] = $currentClass;
        $contentData[29][3] = "生徒氏名　{$studentName}";
        $contentData[30][0] = "成績情報";
        $i = 0;
        for ($i = 0; $i < 4; $i++) {
            $contentData[31 + (8*$i)][ 0] = "年度";
            $contentData[31 + (8*$i)][ 1] = "学年";
            $contentData[31 + (8*$i)][ 2] = "学期";
            $contentData[31 + (8*$i)][25] = "合計";
            $contentData[31 + (8*$i)][26] = "平均";
        }

        //DBからデータ取得
        $scoreData = $this->getDataWithSql($db, knja228aQuery::getCsvScoreData($this, $recRegNo));

        //年度ごとにセット
        $i = 0;
        foreach ($gradeInfo as $key => $gradeVal) {
            $OFFSET_SCORE_ROW = 31 + (8 * $i);

            $contentData[$OFFSET_SCORE_ROW + 1][0] = common::DateConv1($gradeVal['YEAR']."/04/01", 2).'度'; //年度
            $contentData[$OFFSET_SCORE_ROW + 1][1] = $gradeVal['GRADE_NAME'];

            //データを先読みして、学期のコード/名称、科目コード/名称の配列データを作成
            $testMst = array();
            $subClassMst = array();
            foreach ($scoreData as $scoreVal) {
                if ($gradeVal['YEAR'] != $scoreVal['YEAR']) {
                    continue;
                }//異なる年度はスキップ

                //学期
                if (array_key_exists($scoreVal['TESTCD'], $testMst) === false) {
                    $testMst[ $scoreVal['TESTCD'] ] = $scoreVal['TESTITEMNAME'];
                }

                //科目
                if (array_key_exists($scoreVal['SUBCLASSCD'], $subClassMst) === false &&
                        get_count($subClassMst) < 20) {//最大20科目まで
                    $subClassMst[ $scoreVal['SUBCLASSCD'] ] = $scoreVal['SUBCLASSABBV'];
                }
            }

            //行：学期のコード、列：科目コードで二次元配列を作成
            $scoreGrid = array();

            $testMstKeys = array_keys($testMst);
            $subClassMstKeys = array_keys($subClassMst);
            foreach ($testMstKeys as $testMstKey) {
                $scoreGridRow = array();
                foreach ($subClassMstKeys as $subClassMstKey) {
                    $scoreGridRow[$subClassMstKey] = '';
                }

                $scoreGrid[$testMstKey] = $scoreGridRow;
            }

            //点数を埋める
            foreach ($scoreData as $scoreVal) {
                if ($gradeVal['YEAR'] != $scoreVal['YEAR']) {
                    //異なる年度はスキップ
                    continue;
                }

                $scoreGrid[$scoreVal['TESTCD']][$scoreVal['SUBCLASSCD']] = (int)$scoreVal['SCORE'];
            }

            //合計点・平均点を算出
            $arraySum = array();
            $arrayAve = array();
            foreach ($scoreGrid as $keyTest => $valTest) {
                $sumScore   = 0;
                $countScore = 0;
                foreach ($valTest as $keySubclassCd => $score) {
                    if ($score != '') {
                        $sumScore += $score;
                        $countScore++;
                    }
                }

                $arraySum[$keyTest] = $sumScore;
                if ($countScore > 0) {
                    $arrayAve[$keyTest] = round($sumScore / $countScore, 1);//小数第一位まで表示
                } else {
                    $arrayAve[$keyTest] = '0.0';
                }
            }

            //CSVデータにセット(学期名)
            $j = 0;
            foreach ($testMst as $k => $testMstName) {
                $contentData[$OFFSET_SCORE_ROW + ($j + 1)][2] = $testMstName;
                $j++;
            }
            //CSVデータにセット(科目名)
            $j = 0;
            foreach ($subClassMst as $k => $subClassMstName) {
                $contentData[$OFFSET_SCORE_ROW][3 + $j] = $subClassMstName;
                $j++;
            }
            //CSVデータにセット(点数)
            $j = 0;
            foreach ($scoreGrid as $keyTest => $valTest) {
                $k = 0;
                foreach ($valTest as $keySubclassCd => $score) {
                    $contentData[$OFFSET_SCORE_ROW + ($j + 1)][3 + $k] = $score;
                    $k++;
                }
                $j++;
            }
            //CSVデータにセット(合計)
            $j = 0;
            foreach ($arraySum as $k => $valScore) {
                $contentData[$OFFSET_SCORE_ROW + ($j + 1)][25] = $valScore;
                $j++;
            }
            //CSVデータにセット(平均)
            $j = 0;
            foreach ($arrayAve as $k => $valScore) {
                $contentData[$OFFSET_SCORE_ROW + ($j + 1)][26] = $valScore;
                $j++;
            }

            $i++;
        }


        //データ取得＆セット(出欠情報)
        //CSVデータに固定値をセット
        $contentData[64][0] = "出欠情報";
        $contentData[65][0] = "年度";
        $contentData[65][1] = "学年";
        $contentData[65][2] = "授業日数";
        $contentData[65][3] = "出停\n忌引";
        $contentData[65][4] = "留学日数";
        $contentData[65][5] = "欠席日数";
        $contentData[65][6] = "出席日数";
        $contentData[65][7] = "遅刻日数";
        $contentData[65][8] = "早退日数";
        $contentData[65][9] = "備考";

        //DBからデータ取得
        $attendanceData = $this->getDataWithSql($db, knja228aQuery::getCsvAttendanceData($this, $recRegNo));

        //CSVデータにセット
        $i = 0;
        foreach ($attendanceData as $val) {
            $contentData[66 + $i][0] = common::DateConv1($val['YEAR']."/04/01", 2);//年度
                $contentData[66 + $i][1] = $val['GRADE_NAME'];//学年
                $contentData[66 + $i][2] = $val['CLASSDAYS'];//授業日数
                $contentData[66 + $i][3] = $val['MOURNING'];//出停忌引
                $contentData[66 + $i][4] = $val['ABROAD'];//留学日数
                $contentData[66 + $i][5] = $val['SICK'];//欠席日数
                $contentData[66 + $i][6] = $val['PRESENT'];//出席日数
                $contentData[66 + $i][7] = $val['LATE'];//遅刻日数
                $contentData[66 + $i][8] = $val['EARLY'];//早退日数
                $contentData[66 + $i][9] = $val['ATTENDREC_REMARK'];//備考
                $i++;
        }

        //データ取得＆セット(資格取得)
        //CSVデータに固定値をセット
        $contentData[65][11] = "資格取得の記録";
        $contentData[66][11] = "学年";
        $contentData[66][12] = "取得日";
        $contentData[66][13] = "資格名";

        //DBからデータ取得
        $qualifiedData = $this->getDataWithSql($db, knja228aQuery::getCsvQualifiedData($this, $recRegNo));
        $countQualified = get_count($qualifiedData);

        $temp = array();
        $i = 1;
        foreach ($qualifiedData as $val) {
            $temp[] = $val;

            if ($i == 9) {
                //9つで打ち止め
                break;
            }
            $i++;
        }
        $qualifiedData = array_reverse($temp);

        //CSVデータにセット
        $i = 0;
        foreach ($qualifiedData as $val) {
            $contentData[67 + $i][11] = $val['GRADE'];
            $contentData[67 + $i][12] = $this->formatDate($val['REGDDATE']);
            $contentData[67 + $i][13] = $val['SIKAKU'];
            $i++;
        }

        //この時点で後ろの行からデータが無い行をチェック。該当行はデータから削除する
        for ($i = $INIT_ROW - 1; $i >= 0; $i--) {
            //データが無い
            $countBlank = 0;
            for ($j = 0; $j < $INIT_COL; $j++) {
                //ダミー文字は削除
                if ($contentData[$i][$j] == $DUMMY) {
                    $countBlank++;
                }
            }

            if ($countBlank === $INIT_COL) {
                unset($contentData[$i]);
            } else {
                //1つでもデータが入っている行が見つかったら、中断する
                break;
            }
        }

        $contents = "";
        //カンマ区切りの文字列に変換する
        foreach ($contentData as $rowKey => $rowVal) {
            foreach ($rowVal as $colKey => $colVal) {
                //ダミー文字は削除
                if ($colVal == $DUMMY) {
                    $contentData[$rowKey][$colKey] = "";
                }

                //文字列中の改行コードはLFに統一する
                    $contentData[$rowKey][$colKey] = str_replace("\r\n", "\n", $contentData[$rowKey][$colKey]);//CR+LF→LFに統一
                    $contentData[$rowKey][$colKey] = str_replace("\r", "\n", $contentData[$rowKey][$colKey]);//CR   →LFに統一

                    //文字列中に改行コードを含む場合は、文字列自体をダブルクォーテーションで囲む
                if (substr_count($contentData[$rowKey][$colKey], "\n") > 0) {
                    $contentData[$rowKey][$colKey] = "\"" . $contentData[$rowKey][$colKey] . "\"";
                }
            }

            $contents .= (implode(',', $contentData[$rowKey]) . $CSV_EL);
        }

        Query::dbCheckIn($db);
        return $contents;
    }

    /**
     * 画面の検索条件に該当する学籍番号の配列を取得
     */
    private function getTargetRecRegNumbers()
    {
        $kubun = $this->field['KUBUN'];//1:個人、2:クラス
        $selectedCd = $this->selectleft;
        $cds = explode(',', $selectedCd);

        //各要素をシングルクォーテーションで囲む
        if ($kubun == '1') {
            //個人の選択の場合

            $arrCd = array();
            foreach ($cds as $v) {
                //さらにハイフンで区切る
                $v_ =  explode('-', $v);

                //ハイフンの左側が学籍番号
                $arrCd[] = $v_[0];
            }
        } elseif ($kubun == '2') {
            //クラスの選択の場合

            //DB接続
            $db = Query::dbCheckOut();

            //学年・組から学籍番号を取得
            $arrCdTemp = $this->getDataWithSql($db, knja228aQuery::getCsvTargetRecRegNoInClass($this, $cds));

            foreach ($arrCdTemp as $v) {
                $arrCd[] = $v['SCHREGNO'];
            }

            Query::dbCheckIn($db);
        }

        return $arrCd;
    }

    /**
     * CSVダウンロード
     */
    public function getDownloadModel()
    {
        //各生徒のCSV情報を取得
        $targetRecRegNumbers = $this->getTargetRecRegNumbers();
        foreach ($targetRecRegNumbers as $recRegNo) {
            $contentsByStudent[] = $this->getCsvContent($recRegNo);
        }
        $glue = $CSV_EL;//各生徒の間に改行を挟む

        $filename = sprintf("%s.csv", '生徒カルテCSV');
        $contents = implode($glue, $contentsByStudent);
        common::downloadContents($contents, $filename, 'SJIS-win');

        return true;
    }
}
