<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knje380Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $select_opt  = array(); //セレクトデータ
    var $select_MajorOpt  = array(); //セレクトデータ
    var $selectleft;
    var $selectMajorLeft;

    /**
     * メンバ変数の初期化
     */
    function knje380Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("YEAR"             =>  VARS::post("YEAR"),             //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),         //学期コード
                             "QUESTIONNAIRECD"  =>  VARS::post("QUESTIONNAIRECD"),  //学年・クラス
                             "A023_SCHOOL_KIND" =>  VARS::post("A023_SCHOOL_KIND"), //校種
                             "KUBUN"            =>  VARS::post("KUBUN"),            //学年・クラス
                             "GRADE"            =>  VARS::post("GRADE"),            //学年
                             "PRINT1"           =>  VARS::post("PRINT1"),           //チェックボックス
                             "PRINT2"           =>  VARS::post("PRINT2"),           //チェックボックス
                             "PRINT3"           =>  VARS::post("PRINT3"),           //チェックボックス
                             "PRINT4"           =>  VARS::post("PRINT4"),           //チェックボックス
                             "PRINT5"           =>  VARS::post("PRINT5"),           //チェックボックス
                             "PRINT6"           =>  VARS::post("PRINT6"),           //チェックボックス
                             "PRINT7"           =>  VARS::post("PRINT7"),           //チェックボックス
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        $this->selectMajorLeft = VARS::post("selectMajorLeft");
        if ($this->cmd == 'knje380' || $this->cmd == '') $this->select_opt = array();
        if ($this->cmd == 'knje380' || $this->cmd == '') $this->select_MajorOpt = array();
    }

    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        $this->schoolMst = $db->getRow(knje380Query::getSchoolMst($this), DB_FETCHMODE_ASSOC);

        $this->outPutData = array();
        $selectMajorLeft = explode(",", $this->selectMajorLeft);
        foreach ($selectMajorLeft as $key => $majorCd) {
            $this->setOutPutStudent($db, $majorCd);

            if ($this->cmd == "csv1") {
                $filename = "県報告用進路状況調査表";
                list($getContents, $hasData) = $this->downLoadData1($db, $majorCd);
            } else if ($this->cmd == "csv2") {
                $filename = "新規高等学校卒業者の進路状況";
                list($getContents, $hasData) = $this->downLoadData2($db, $majorCd);
            } else if ($this->cmd == "csv3") {
                $filename = "新規高等学校卒業者の地域別就職状況(学校紹介分)";
                list($getContents, $hasData) = $this->downLoadData3($db, $majorCd);
            } else if ($this->cmd == "csv4") {
                $filename = "新規学校卒業者の産業別就職状況(学校紹介分)";
                list($getContents, $hasData) = $this->downLoadData4($db, $majorCd);
            } else if ($this->cmd == "csv5") {
                $filename = "新規学校卒業者の職業別・規模別就職状況(学校紹介分)";
                list($getContents, $hasData) = $this->downLoadData5($db, $majorCd);
            } else if ($this->cmd == "csv6") {
                $filename = "進路希望調査";
                list($getContents, $hasData) = $this->downLoadData6($db, $majorCd);
            } else if ($this->cmd == "csv7") {
                $filename = "中学校別進路一覧";
                list($getContents, $hasData) = $this->downLoadData7($db, $majorCd);
            }

            $contents .= $getContents."\n\n\n\n\n";

            $this->outPutData = array();
        }

        //DB切断
        Query::dbCheckIn($db);
        //ＣＳＶ出力
        if ($hasData){
            $couse="生徒指導要録";
            $filename = sprintf("%s.csv",
                                         $filename
                                );
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    function setOutPutStudent($db, $majorCd) {
        $selectleft = explode(",", $this->selectleft);
        foreach ($selectleft as $key => $selected) {
            $query = knje380Query::getStudentSql($this, $selected, $majorCd);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $aft0 = $this->getAft($db, $row["SCHREGNO"], "0");
                $row["IS_SINGAKU"] = $aft0["SENKOU_KIND"] == "0" ? "ON" : $row["IS_SINGAKU"];
                $row["IS_SHUSYOKU"] = $aft0["SENKOU_KIND"] == "1" ? "ON" : $row["IS_SHUSYOKU"];

                $aft1 = $this->getAft($db, $row["SCHREGNO"], "1");
                $row["IS_SINGAKU"] = $aft1["SENKOU_KIND"] == "0" ? "ON" : $row["IS_SINGAKU"];
                $row["IS_SHUSYOKU"] = $aft1["SENKOU_KIND"] == "1" ? "ON" : $row["IS_SHUSYOKU"];

                $hope = $this->getHope($db, $row);
                $row["IS_SINGAKU_HOPE"] = $hope["COURSE_KIND"] == "1" ? "ON" : $row["IS_SINGAKU_HOPE"];
                $row["IS_SHUSYOKU_HOPE"] = $hope["COURSE_KIND"] == "2" ? "ON" : $row["IS_SHUSYOKU_HOPE"];
                $row["IS_KAJI_HOPE"] = $hope["COURSE_KIND"] == "3" ? "ON" : $row["IS_KAJI_HOPE"];
                $row["IS_MITEI_HOPE"] = $hope["COURSE_KIND"] == "4" ? "ON" : $row["IS_MITEI_HOPE"];
                $row["AFT_GRAD_COURSE_DAT_SIN"] = $aft0;
                $row["AFT_GRAD_COURSE_DAT_SHU"] = $aft1;
                $row["COURSE_HOPE_DAT"] = $hope;
                $this->outPutData[] = $row;

            }
            $result->free();
        }
    }

    function getAft($db, $schregNo, $senkou) {
        $query = knje380Query::getAftSql($schregNo, $senkou);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        return $row;
    }

    function getHope($db, $datas) {
        $query = knje380Query::getHopeSql($this, $datas["SCHREGNO"]);
        $result = $db->query($query);
        $retRow;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["COURSE_KIND"] == "1" && $datas["IS_SHUSYOKU"]) {
                break;
            }
            $retRow = $row;
        }

        return $retRow;
    }

    function downLoadData1($db, $majorCd) {
        $gradPlan = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $eduPlan = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $hopeSt = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $hopeSi = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $hopeSo = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $hopeMt = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $hopeMi = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $hopeMo = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $hopePt = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $hopePi = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $hopePo = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $hopeTt = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $hopeTi = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $hopeTo = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $otherPlanY = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $otherPlanN = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $aftSt = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $aftSi = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $aftSo = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $aftMt = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $aftMi = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $aftMo = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $aftPt = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $aftPi = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $aftPo = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $aftTt = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $aftTi = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $aftTo = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $aftPlan = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $noAftTt = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $noAftTi = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $noAftTo = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $noHope = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);

        $gradPlan = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        foreach ($this->outPutData as $key => $student) {
            $gradPlan = $this->setCnt($gradPlan, $student);
            // 進学希望
            if ($student["IS_SINGAKU_HOPE"] || ($student["IS_SINGAKU"] && !$student["IS_SHUSYOKU"])) {
                $eduPlan = $this->setCnt($eduPlan, $student);
            } else {
                // 就職希望：無 or 未
                if (!$student["COURSE_HOPE_DAT"]["COURSE_KIND"] || $student["IS_MITEI_HOPE"]) {
                    $otherPlanN = $this->setCnt($otherPlanN, $student);
                    if (!$student["IS_SHUSYOKU"]) {
                        $noHope = $this->setCnt($noHope, $student);
                    }
                } else {
                    // 就職希望：家事手伝い
                    if ($student["IS_KAJI_HOPE"]) {
                        $otherPlanY = $this->setCnt($otherPlanY, $student);
                        if (!$student["IS_SHUSYOKU"]) {
                            $noHope = $this->setCnt($noHope, $student);
                        }
                    } else {
                        // 就職希望合計
                        $hopeTt = $this->setCnt($hopeTt, $student);
                        if ($student["COURSE_HOPE_DAT"]["WORK_AREA1"] == "1") {
                            $hopeTi = $this->setCnt($hopeTi, $student);
                        } else {
                            $hopeTo = $this->setCnt($hopeTo, $student);
                        }
                        // 就職希望紹介：学校
                        if ($student["COURSE_HOPE_DAT"]["INTRODUCTION_DIV1"] == "1") {
                            $hopeSt = $this->setCnt($hopeSt, $student);
                            if ($student["COURSE_HOPE_DAT"]["WORK_AREA1"] == "1") {
                                $hopeSi = $this->setCnt($hopeSi, $student);
                            } else {
                                $hopeSo = $this->setCnt($hopeSo, $student);
                            }
                        }
                        // 就職希望紹介：自己・縁故
                        if ($student["COURSE_HOPE_DAT"]["INTRODUCTION_DIV1"] == "2") {
                            $hopeMt = $this->setCnt($hopeMt, $student);
                            if ($student["COURSE_HOPE_DAT"]["WORK_AREA1"] == "1") {
                                $hopeMi = $this->setCnt($hopeMi, $student);
                            } else {
                                $hopeMo = $this->setCnt($hopeMo, $student);
                            }
                        }
                        // 就職希望紹介：公務員
                        if ($student["COURSE_HOPE_DAT"]["INTRODUCTION_DIV1"] == "3") {
                            $hopePt = $this->setCnt($hopePt, $student);
                            if ($student["COURSE_HOPE_DAT"]["WORK_AREA1"] == "1") {
                                $hopePi = $this->setCnt($hopePi, $student);
                            } else {
                                $hopePo = $this->setCnt($hopePo, $student);
                            }
                        }
                    }
                }
                // 就職内定(確定者)
                if ($student["IS_SHUSYOKU"]) {
                    if ($student["IS_SINGAKU"]) {
                        $aftPlan = $this->setCnt($aftPlan, $student);
                    } else {
                        // 就職内定合計
                        $aftTt = $this->setCnt($aftTt, $student);
                        if ($student["AFT_GRAD_COURSE_DAT_SHU"]["PREF_CD"] == $this->schoolMst["PREF_CD"]) {
                            $aftTi = $this->setCnt($aftTi, $student);
                        } else {
                            $aftTo = $this->setCnt($aftTo, $student);
                        }
                        // 就職内定紹介：学校
                        if ($student["AFT_GRAD_COURSE_DAT_SHU"]["INTRODUCTION_DIV"] == "1") {
                            $aftSt = $this->setCnt($aftSt, $student);
                            if ($student["AFT_GRAD_COURSE_DAT_SHU"]["PREF_CD"] == $this->schoolMst["PREF_CD"]) {
                                $aftSi = $this->setCnt($aftSi, $student);
                            } else {
                                $aftSo = $this->setCnt($aftSo, $student);
                            }
                        }
                        // 就職内定紹介：自己・縁故
                        if ($student["AFT_GRAD_COURSE_DAT_SHU"]["INTRODUCTION_DIV"] == "2") {
                            $aftMt = $this->setCnt($aftMt, $student);
                            if ($student["AFT_GRAD_COURSE_DAT_SHU"]["PREF_CD"] == $this->schoolMst["PREF_CD"]) {
                                $aftMi = $this->setCnt($aftMi, $student);
                            } else {
                                $aftMo = $this->setCnt($aftMo, $student);
                            }
                        }
                        // 就職内定紹介：公務員
                        if ($student["AFT_GRAD_COURSE_DAT_SHU"]["INTRODUCTION_DIV"] == "3") {
                            $aftPt = $this->setCnt($aftPt, $student);
                            if ($student["AFT_GRAD_COURSE_DAT_SHU"]["PREF_CD"] == $this->schoolMst["PREF_CD"]) {
                                $aftPi = $this->setCnt($aftPi, $student);
                            } else {
                                $aftPo = $this->setCnt($aftPo, $student);
                            }
                        }
                    }
                } else {
                    if ($student["COURSE_HOPE_DAT"]["COURSE_KIND"] && !$student["IS_MITEI_HOPE"] && !$student["IS_KAJI_HOPE"]) {
                        $noHope = $this->setCnt($noHope, $student);
                    }
                    $noAftTt = $this->setCnt($noAftTt, $student);
                    if ($student["COURSE_HOPE_DAT"]["COURSE_KIND"] && $student["COURSE_HOPE_DAT"]["WORK_AREA1"] == "1") {
                        $noAftTi = $this->setCnt($noAftTi, $student);
                    } else {
                        $noAftTo = $this->setCnt($noAftTo, $student);
                    }
                }
            }
            $hasData = true;
        }
        $majorName = $majorCd == "9999" ? "全学科" : $db->getOne(knje380Query::getMajorName($majorCd));

        $retContents  = "県報告用進路状況調査表,,,,,,,".common::DateConv1(str_replace("-", "/", CTRL_DATE), 0)."　現在\n";
        $retContents .= ",,,,,,,　学校名　".$this->schoolMst["SCHOOLNAME1"]."\n";
        $retContents .= "性別,卒業予定者数,進路希望者数\n";
        $retContents .= "男,".$gradPlan["MAN_CNT"].",".$eduPlan["MAN_CNT"]."\n";
        $retContents .= "女,".$gradPlan["WOMAN_CNT"].",".$eduPlan["WOMAN_CNT"]."\n";
        $retContents .= "計,".$gradPlan["TOTAL_CNT"].",".$eduPlan["TOTAL_CNT"]."\n";
        $retContents .= ",,,,,,,　学科：　".$majorName."\n";

        $retContents .= "\n";
        $retContents .= "性別,就職希望者数,,,,,,,,,,,,その他\n";
        $retContents .= ",学校紹介,,,自己・縁故,,,公務員,,,合計,,,卒業後の計画,\n";
        $retContents .= ",計,県内,県外,計,県内,県外,計,県内,県外,計,県内,県外,有,無\n";
        $retContents .= "男,".$hopeSt["MAN_CNT"].",".$hopeSi["MAN_CNT"].",".$hopeSo["MAN_CNT"].",".$hopeMt["MAN_CNT"].",".$hopeMi["MAN_CNT"].",".$hopeMo["MAN_CNT"].",".$hopePt["MAN_CNT"].",".$hopePi["MAN_CNT"].",".$hopePo["MAN_CNT"].",".$hopeTt["MAN_CNT"].",".$hopeTi["MAN_CNT"].",".$hopeTo["MAN_CNT"].",".$otherPlanY["MAN_CNT"].",".$otherPlanN["MAN_CNT"]."\n";
        $retContents .= "女,".$hopeSt["WOMAN_CNT"].",".$hopeSi["WOMAN_CNT"].",".$hopeSo["WOMAN_CNT"].",".$hopeMt["WOMAN_CNT"].",".$hopeMi["WOMAN_CNT"].",".$hopeMo["WOMAN_CNT"].",".$hopePt["WOMAN_CNT"].",".$hopePi["WOMAN_CNT"].",".$hopePo["WOMAN_CNT"].",".$hopeTt["WOMAN_CNT"].",".$hopeTi["WOMAN_CNT"].",".$hopeTo["WOMAN_CNT"].",".$otherPlanY["WOMAN_CNT"].",".$otherPlanN["WOMAN_CNT"]."\n";
        $retContents .= "計,".$hopeSt["TOTAL_CNT"].",".$hopeSi["TOTAL_CNT"].",".$hopeSo["TOTAL_CNT"].",".$hopeMt["TOTAL_CNT"].",".$hopeMi["TOTAL_CNT"].",".$hopeMo["TOTAL_CNT"].",".$hopePt["TOTAL_CNT"].",".$hopePi["TOTAL_CNT"].",".$hopePo["TOTAL_CNT"].",".$hopeTt["TOTAL_CNT"].",".$hopeTi["TOTAL_CNT"].",".$hopeTo["TOTAL_CNT"].",".$otherPlanY["TOTAL_CNT"].",".$otherPlanN["TOTAL_CNT"]."\n";

        $retContents .= "\n";
        $retContents .= "性別,就職内定者数者数,,,,,,,,,,,,その他\n";
        $retContents .= ",学校紹介,,,自己・縁故,,,公務員,,,合計,,,卒業後の計画,\n";
        $retContents .= ",計,県内,県外,計,県内,県外,計,県内,県外,計,県内,県外,進学就職\n";
        $retContents .= "男,".$aftSt["MAN_CNT"].",".$aftSi["MAN_CNT"].",".$aftSo["MAN_CNT"].",".$aftMt["MAN_CNT"].",".$aftMi["MAN_CNT"].",".$aftMo["MAN_CNT"].",".$aftPt["MAN_CNT"].",".$aftPi["MAN_CNT"].",".$aftPo["MAN_CNT"].",".$aftTt["MAN_CNT"].",".$aftTi["MAN_CNT"].",".$aftTo["MAN_CNT"].",".$aftPlan["MAN_CNT"]."\n";
        $retContents .= "女,".$aftSt["WOMAN_CNT"].",".$aftSi["WOMAN_CNT"].",".$aftSo["WOMAN_CNT"].",".$aftMt["WOMAN_CNT"].",".$aftMi["WOMAN_CNT"].",".$aftMo["WOMAN_CNT"].",".$aftPt["WOMAN_CNT"].",".$aftPi["WOMAN_CNT"].",".$aftPo["WOMAN_CNT"].",".$aftTt["WOMAN_CNT"].",".$aftTi["WOMAN_CNT"].",".$aftTo["WOMAN_CNT"].",".$aftPlan["WOMAN_CNT"]."\n";
        $retContents .= "計,".$aftSt["TOTAL_CNT"].",".$aftSi["TOTAL_CNT"].",".$aftSo["TOTAL_CNT"].",".$aftMt["TOTAL_CNT"].",".$aftMi["TOTAL_CNT"].",".$aftMo["TOTAL_CNT"].",".$aftPt["TOTAL_CNT"].",".$aftPi["TOTAL_CNT"].",".$aftPo["TOTAL_CNT"].",".$aftTt["TOTAL_CNT"].",".$aftTi["TOTAL_CNT"].",".$aftTo["TOTAL_CNT"].",".$aftPlan["TOTAL_CNT"]."\n";

        $retContents .= "\n";
        $retContents .= "性別,就職希望者の内未内定者,,,進学希望者以外の進路未決定者,\n";
        $retContents .= ",計,県内,県外,\n";
        $retContents .= "男,".$noAftTt["MAN_CNT"].",".$noAftTi["MAN_CNT"].",".$noAftTo["MAN_CNT"].",".$noHope["MAN_CNT"]."\n";
        $retContents .= "女,".$noAftTt["WOMAN_CNT"].",".$noAftTi["WOMAN_CNT"].",".$noAftTo["WOMAN_CNT"].",".$noHope["WOMAN_CNT"]."\n";
        $retContents .= "計,".$noAftTt["TOTAL_CNT"].",".$noAftTi["TOTAL_CNT"].",".$noAftTo["TOTAL_CNT"].",".$noHope["TOTAL_CNT"]."\n";

        return array($retContents, $hasData);
    }

    function downLoadData2($db, $majorCd) {
        $hasData = false;
        $gradPlan = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $singaku = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $shushokuS = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $shushokuP = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $shushokuM = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $nouryokuKaihtu = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $senmon = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $sonota = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        foreach ($this->outPutData as $key => $student) {
            $gradPlan = $this->setCnt($gradPlan, $student);
            if ($student["IS_SHUSYOKU"]) {
                if ($student["AFT_GRAD_COURSE_DAT_SHU"]["INTRODUCTION_DIV"] == "1") {
                    $shushokuS = $this->setCnt($shushokuS, $student);
                } else if ($student["AFT_GRAD_COURSE_DAT_SHU"]["INTRODUCTION_DIV"] == "3") {
                    $shushokuP = $this->setCnt($shushokuP, $student);
                } else if ($student["AFT_GRAD_COURSE_DAT_SHU"]["INTRODUCTION_DIV"] == "2") {
                    $shushokuM = $this->setCnt($shushokuM, $student);
                } else {
                    $sonota = $this->setCnt($sonota, $student);
                }
            } else if ($student["IS_SINGAKU"]) {
                if ($student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "01" ||
                    $student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "02" ||
                    $student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "03" ||
                    $student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "04" ||
                    $student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "05"
                ) {
                    $singaku = $this->setCnt($singaku, $student);
                } else if ($student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "06" ||
                           $student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "07"
                ) {
                    $senmon = $this->setCnt($senmon, $student);
                } else if ($student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "08" ||
                           $student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "09"
                ) {
                    $nouryokuKaihtu = $this->setCnt($nouryokuKaihtu, $student);
                } else {
                    $sonota = $this->setCnt($sonota, $student);
                }
            } else {
                $sonota = $this->setCnt($sonota, $student);
            }
            $hasData = true;
        }

        $majorName = $majorCd == "9999" ? "全学科" : $db->getOne(knje380Query::getMajorName($majorCd));

        $year = CTRL_YEAR;
        $month = "04";
        $day = "01";
        $retContents  = "新規高等学校卒業者の進路状況\n";
        $retContents .= ",　学校名　".$this->schoolMst["SCHOOLNAME1"]."\n";
        $retContents .= common::Calc_Wareki($year, $month, $day)."度　卒業者\n";
        $retContents .= ",　学科：　".$majorName."\n";
        $retContents .= ",項目,計,男,女\n";
        $retContents .= ",卒業者数,".$gradPlan["TOTAL_CNT"].",".$gradPlan["MAN_CNT"].",".$gradPlan["WOMAN_CNT"]."\n";
        $retContents .= "内訳,進学者数,".$singaku["TOTAL_CNT"].",".$singaku["MAN_CNT"].",".$singaku["WOMAN_CNT"]."\n";
        $retContents .= ",学校紹介就職者数,".$shushokuS["TOTAL_CNT"].",".$shushokuS["MAN_CNT"].",".$shushokuS["WOMAN_CNT"]."\n";
        $retContents .= ",公務員就職者,".$shushokuP["TOTAL_CNT"].",".$shushokuP["MAN_CNT"].",".$shushokuP["WOMAN_CNT"]."\n";
        $retContents .= ",自己・縁故等就職者,".$shushokuM["TOTAL_CNT"].",".$shushokuM["MAN_CNT"].",".$shushokuM["WOMAN_CNT"]."\n";
        $retContents .= ",職業能力開発施設入校者数,".$nouryokuKaihtu["TOTAL_CNT"].",".$nouryokuKaihtu["MAN_CNT"].",".$nouryokuKaihtu["WOMAN_CNT"]."\n";
        $retContents .= ",各種専門学校入校者,".$senmon["TOTAL_CNT"].",".$senmon["MAN_CNT"].",".$senmon["WOMAN_CNT"]."\n";
        $retContents .= ",その他,".$sonota["TOTAL_CNT"].",".$sonota["MAN_CNT"].",".$sonota["WOMAN_CNT"]."\n";

        return array($retContents, $hasData);
    }

    function downLoadData3($db, $majorCd) {

        $majorName = $majorCd == "9999" ? "全学科" : $db->getOne(knje380Query::getMajorName($majorCd));

        $year = CTRL_YEAR;
        $month = "04";
        $day = "01";
        $retContents  = "新規高等学校卒業者の地域別就職状況(学校紹介分)\n";
        $retContents .= ",,,　学校名　".$this->schoolMst["SCHOOLNAME1"]."\n";
        $retContents .= common::Calc_Wareki($year, $month, $day)."度　卒業者\n";
        $retContents .= ",,,　学科：　".$majorName."\n";
        $retContents .= "都道府県,計,男,女\n";

        $hasData = false;
        $prefMap = $this->getPrefMap($db);
        $totalData = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $kengaiTotal = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        foreach ($this->outPutData as $key => $student) {
            if ($student["IS_SHUSYOKU"] && $student["AFT_GRAD_COURSE_DAT_SHU"]["INTRODUCTION_DIV"] == "1") {
                $prefCd = $student["AFT_GRAD_COURSE_DAT_SHU"]["PREF_CD"];
                if (array_key_exists($prefCd, $prefMap)) {
                    $totalData = $this->setCnt($totalData, $student);
                    if ($prefMap[$prefCd]["IS_SCHOOL_PREF"]) {
                        $cityCd = $student["AFT_GRAD_COURSE_DAT_SHU"]["CITY_CD"];
                        if (array_key_exists($cityCd, $prefMap[$prefCd]["CITY_DATA"])) {
                            $prefMap[$prefCd]["CITY_DATA"][$cityCd]["PRINT_DATA"] = $this->setCnt($prefMap[$prefCd]["CITY_DATA"][$cityCd]["PRINT_DATA"], $student);
                        } else {
                            $prefMap[$prefCd]["PRINT_DATA"] = $this->setCnt($prefMap[$prefCd]["PRINT_DATA"], $student);
                        }
                    } else {
                        $prefMap[$prefCd]["PRINT_DATA"] = $this->setCnt($prefMap[$prefCd]["PRINT_DATA"], $student);
                        $kengaiTotal = $this->setCnt($kengaiTotal, $student);
                    }
                }
            }
            $hasData = true;
        }

        foreach ($prefMap as $key => $prefData) {
            if (!$prefData["IS_SCHOOL_PREF"]) {
                $retContents .= $prefData["PREF_NAME"].",".$prefData["PRINT_DATA"]["TOTAL_CNT"].",".$prefData["PRINT_DATA"]["MAN_CNT"].",".$prefData["PRINT_DATA"]["WOMAN_CNT"]."\n";
            } else {
                $retContents2  = $prefData["PREF_NAME"].",".$prefData["PRINT_DATA"]["TOTAL_CNT"].",".$prefData["PRINT_DATA"]["MAN_CNT"].",".$prefData["PRINT_DATA"]["WOMAN_CNT"]."\n";
                if (is_array($prefData["CITY_DATA"])) {
                    foreach ($prefData["CITY_DATA"] as $cityKey => $cityData) {
                        $retContents2 .= $cityData["CITY_NAME"].",".$cityData["PRINT_DATA"]["TOTAL_CNT"].",".$cityData["PRINT_DATA"]["MAN_CNT"].",".$cityData["PRINT_DATA"]["WOMAN_CNT"]."\n";
                    }
                }
            }
        }
        $kengaiContents = "県外小計,".$kengaiTotal["TOTAL_CNT"].",".$kengaiTotal["MAN_CNT"].",".$kengaiTotal["WOMAN_CNT"]."\n";
        $retContents .= $kengaiContents.$retContents2;
        $retContents .= "合計,".$totalData["TOTAL_CNT"].",".$totalData["MAN_CNT"].",".$totalData["WOMAN_CNT"]."\n";

        return array($retContents, $hasData);
    }

    function downLoadData4($db, $majorCd) {
        $hasData = false;

        $printAllDatas = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $printMap = array();
        foreach ($this->outPutData as $key => $student) {
            if ($student["IS_SHUSYOKU"] && $student["AFT_GRAD_COURSE_DAT_SHU"]["INTRODUCTION_DIV"] == "1") {
                $printAllDatas = $this->setCnt($printAllDatas, $student);
                $printData = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
                $lCd = $student["AFT_GRAD_COURSE_DAT_SHU"]["INDUSTRY_LCD"];
                $mCd = $student["AFT_GRAD_COURSE_DAT_SHU"]["INDUSTRY_MCD"];
                //大＋中分類毎に合算
                $lmCd = $lCd.$mCd;
                if (array_key_exists($lmCd, $printMap)) {
                    $printData = $printMap[$lmCd];
                }
                $printData = $this->setCnt($printData, $student);
                $printMap[$lmCd] = $printData;
            }
        }

        $printDataAll = $this->getPrintDataAll($db, $printMap);

        $majorName = $majorCd == "9999" ? "全学科" : $db->getOne(knje380Query::getMajorName($majorCd));

        $year = CTRL_YEAR;
        $month = "04";
        $day = "01";
        $retContents  = "新規学校卒業者の産業別就職状況(学校紹介分)\n";
        $retContents .= ",　学校名　".$this->schoolMst["SCHOOLNAME1"]."\n";
        $retContents .= common::Calc_Wareki($year, $month, $day)."度　卒業者,　学科：　".$majorName."\n";
        $retContents .= "項目,就職者数\n";
        $retContents .= ",計,男,女\n";
        foreach ($printDataAll as $key => $printLDatas) {
            $retContents .= $this->getLcd($printLDatas["LCD_MAP"])." ".$printLDatas["TITLE_NAME"]."　　　";
            $retContents .= $this->getMcd($printLDatas["MCD_MAP"]).",";
            $retContents .= $printLDatas["PRINT_DATA"]["TOTAL_CNT"].",";
            $retContents .= $printLDatas["PRINT_DATA"]["MAN_CNT"].",";
            $retContents .= $printLDatas["PRINT_DATA"]["WOMAN_CNT"]."\n";

            foreach ($printLDatas["M_MAP"] as $mKey => $printMDatas) {
                $retContents .= "　　".$this->getMcd2($printMDatas["MCD_MAP"])." ".$printMDatas["TITLE_NAME"].",";
                $retContents .= $printMDatas["PRINT_DATA"]["TOTAL_CNT"].",";
                $retContents .= $printMDatas["PRINT_DATA"]["MAN_CNT"].",";
                $retContents .= $printMDatas["PRINT_DATA"]["WOMAN_CNT"]."\n";
            }
            $hasData = true;
        }
        $retContents .= "合計,".$printAllDatas["TOTAL_CNT"].",".$printAllDatas["MAN_CNT"].",".$printAllDatas["WOMAN_CNT"]."\n";

        return array($retContents, $hasData);
    }

    function downLoadData5($db, $majorCd) {

        $hasData = false;
        $KIBO29 = 29;
        $KIBO99 = 99;
        $KIBO299 = 299;
        $KIBO499 = 499;
        $KIBO999 = 999;
        $KIBO1000 = 1000;

        $printKoumoku = $this->getPrintKoumoku();
        $printKibo = $this->getPrintKibo();
        $printGassan = $this->getGassan();
        foreach ($this->outPutData as $key => $student) {
            if ($student["IS_SHUSYOKU"] && $student["AFT_GRAD_COURSE_DAT_SHU"]["INTRODUCTION_DIV"] == "1") {
                $printData = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
                $printData = $this->setCnt($printData, $student);

                $lCd = $student["AFT_GRAD_COURSE_DAT_SHU"]["JOBTYPE_LCD"];
                $prefCd = $student["AFT_GRAD_COURSE_DAT_SHU"]["PREF_CD"];

                $totalLDatas = $printKoumoku["TOTAL"];
                $totalLDatas = $this->setLData($totalLDatas, $printData, $prefCd);
                $printKoumoku["TOTAL"] = $totalLDatas;

                if (array_key_exists($lCd, $printKoumoku)) {
                    $printLDatas = $printKoumoku[$lCd];
                    $printLDatas = $this->setLData($printLDatas, $printData, $prefCd);
                    $printKoumoku[$lCd] = $printLDatas;
                    if (array_key_exists($lCd, $printGassan)) {
                        $gassan = $printGassan[$lCd];
                        $printGassanDatas = $printKoumoku[$gassan];
                        $printGassanDatas = $this->setLData($printGassanDatas, $printData, $prefCd);
                        $printKoumoku[$gassan] = $printGassanDatas;
                    }
                }
                $soninZu = $student["AFT_GRAD_COURSE_DAT_SHU"]["SONINZU"];
                if ($KIBO29 >= $soninZu) {
                    $kiboKey = $KIBO29;
                    $printLDatas = $printKibo[$kiboKey];
                    $printLDatas = $this->setLData($printLDatas, $printData, $prefCd);
                    $printKibo[$kiboKey] = $printLDatas;
                } else if ($KIBO99 >= $soninZu) {
                    $kiboKey = $KIBO99;
                    $printLDatas = $printKibo[$kiboKey];
                    $printLDatas = $this->setLData($printLDatas, $printData, $prefCd);
                    $printKibo[$kiboKey] = $printLDatas;
                } else if ($KIBO499 >= $soninZu) {
                    $kiboKey = $KIBO499;
                    $printLDatas = $printKibo[$kiboKey];
                    $printLDatas = $this->setLData($printLDatas, $printData, $prefCd);
                    $printKibo[$kiboKey] = $printLDatas;
                } else if ($KIBO999 >= $soninZu) {
                    $kiboKey = $KIBO999;
                    $printLDatas = $printKibo[$kiboKey];
                    $printLDatas = $this->setLData($printLDatas, $printData, $prefCd);
                    $printKibo[$kiboKey] = $printLDatas;
                } else if ($KIBO1000 <= $soninZu) {
                    $kiboKey = $KIBO1000;
                    $printLDatas = $printKibo[$kiboKey];
                    $printLDatas = $this->setLData($printLDatas, $printData, $prefCd);
                    $printKibo[$kiboKey] = $printLDatas;
                }
            }
        }

        $majorName = $majorCd == "9999" ? "全学科" : $db->getOne(knje380Query::getMajorName($majorCd));

        $year = CTRL_YEAR;
        $month = "04";
        $day = "01";
        $retContents  = ",新規学校卒業者の職業別・規模別就職状況(学校紹介分)\n";
        $retContents .= ",,　学校名　".$this->schoolMst["SCHOOLNAME1"]."\n";
        $retContents .= ",".common::Calc_Wareki($year, $month, $day)."度　卒業者,　学科：　".$majorName."\n";
        $retContents .= ",職業別,,就職者\n";
        $retContents .= ",,,計,男,女\n";

        foreach ($printKoumoku as $key => $printLDatas) {
            $retContents .= ",".$printLDatas["TITLE"].",".$printLDatas["TITLE2"].",".$printLDatas["L_ALL_PRINTDATA"]["TOTAL_CNT"].",".$printLDatas["L_ALL_PRINTDATA"]["MAN_CNT"].",".$printLDatas["L_ALL_PRINTDATA"]["WOMAN_CNT"]."\n";
            $retContents .= ",".$printLDatas["SUT_TITLE"].",".$printLDatas["SUB_TITLE2"].",".$printLDatas["L_IN_PRINTDATA"]["TOTAL_CNT"].",".$printLDatas["L_IN_PRINTDATA"]["MAN_CNT"].",".$printLDatas["L_IN_PRINTDATA"]["WOMAN_CNT"]."\n";
            $hasData = true;
        }
        $retContents .= "規模別";
        foreach ($printKibo as $key => $printLDatas) {
            $retContents .= ",".$printLDatas["TITLE"].",,".$printLDatas["L_ALL_PRINTDATA"]["TOTAL_CNT"].",".$printLDatas["L_ALL_PRINTDATA"]["MAN_CNT"].",".$printLDatas["L_ALL_PRINTDATA"]["WOMAN_CNT"]."\n";
        }

        return array($retContents, $hasData);
    }

    function downLoadData6($db, $majorCd) {
        $hasData = false;

        $gradeList = array();
        $befGrade = "";
        foreach ($this->outPutData as $key => $student) {
            if ($befGrade && $befGrade != $student["GRADE"]) {
                $retContents .= $this->printOut($db, $gradeList, $befGrade, $majorCd);
                $gradeList = array();
            }
            $gradeList[] = $student;
            $befGrade = $student["GRADE"];
            $hasData = true;
        }
        if ($befGrade != "") {
            $retContents .= $this->printOut($db, $gradeList, $befGrade, $majorCd);
        }

        return array($retContents, $hasData);
    }

    function downLoadData7($db, $majorCd) {
        return array("", false);
    }

    function setCnt($gradPlan, $student) {
        $gradPlan["TOTAL_CNT"]++;
        if ($student["SEX_CD"] == "1") {
            $gradPlan["MAN_CNT"]++;
        } else {
            $gradPlan["WOMAN_CNT"]++;
        }
        return $gradPlan;
    }

    function getPrefMap($db) {
        $retMap = array();
        $prefSql = "SELECT * FROM PREF_MST ORDER BY PREF_CD ";

        $result = $db->query($prefSql);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $prefCd = $row["PREF_CD"];
            $row["IS_SCHOOL_PREF"] = $prefCd == $this->schoolMst["PREF_CD"] ? "ON" : "";
            $row["PRINT_DATA"] = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
            if ($row["IS_SCHOOL_PREF"]) {
                $row["CITY_DATA"] = $this->setCityMap($db);
            }
            $retMap[$prefCd] = $row;
        }
        $result->free();

        return $retMap;
    }

    function setCityMap($db) {
        $retMap;

        $citySql = "SELECT * FROM CITY_MST WHERE PREF_CD = '".$this->schoolMst["PREF_CD"]."' AND CITY_FLG1 = '1' ORDER BY CITY_CD ";
        $resutl2 = $db->query($citySql);
        while($row = $resutl2->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["PRINT_DATA"] = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
            $retMap[$row["CITY_CD"]] = $row;
        }
        $resutl2->free();

        return $retMap;
    }

    function getPrintDataAll($db, $printMap) {
        $retList = array();

        $induSql = knje380Query::getIndustrySql();

        $befLcd = "";
        $befGrpLcd = "";

        $printLDatas;
        $result = $db->query($induSql);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $lCd = $row["INDUSTRY_LCD"];
            $industryLname = $row["INDUSTRY_LNAME"];
            $lName = $row["L_GROUPNAME"] ? $row["L_GROUPNAME"] : $industryLname;
            $lGrpCd = $row["L_GROUPCD"] ? $row["L_GROUPCD"] : "";

            $mCd = $row["INDUSTRY_MCD"];
            $industryMname = $row["INDUSTRY_MNAME"];
            $mName = $row["M_GROUPNAME"] ? $row["M_GROUPNAME"] : $industryMname;

            $noOutput = $row["NO_OUTPUT"] ? "ON" : "";

            $printData = $printMap[$lCd.$mCd];
            //大分類の改行
            if (($befLcd != $lCd && $lGrpCd == "") ||
                ($befLcd != $lCd && $befGrpLcd != $lGrpCd)
            ) {
                if (is_array($printLDatas)) {
                    $retList[] = $printLDatas;
                }
                $printLDatas = array();
                $printLDatas["PRINT_DATA"] = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
                $printLDatas["M_MAP"] = array();
            }

            //大分類のデータ作成
            $printLDatas["TITLE_NAME"] = $lName;
            $printLDatas["LCD_MAP"][$lCd] = $lCd;
            $printLDatas["MCD_MAP"][$mCd] = $mCd;
            if (is_array($printData)) {
                $printLDatas["PRINT_DATA"]["MAN_CNT"] += $printData["MAN_CNT"];
                $printLDatas["PRINT_DATA"]["WOMAN_CNT"] += $printData["WOMAN_CNT"];
                $printLDatas["PRINT_DATA"]["TOTAL_CNT"] += $printData["TOTAL_CNT"];
            }

            //出力ありの中分類
            if (!$noOutput) {
                //中分類のデータ作成
                $setMcd = $row["M_GROUPCD"] ? $row["M_GROUPCD"] : $mCd;
                $printMDatas["PRINT_DATA"] = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
                if (array_key_exists($setMcd, $printLDatas["M_MAP"])) {
                    $printMDatas = $printLDatas["M_MAP"][$setMcd];
                }
                $printMDatas["TITLE_NAME"] = $mName;
                $printMDatas["MCD_MAP"][$mCd] = $mCd;
                if (is_array($printData)) {
                    $printMDatas["PRINT_DATA"]["MAN_CNT"] += $printData["MAN_CNT"];
                    $printMDatas["PRINT_DATA"]["WOMAN_CNT"] += $printData["WOMAN_CNT"];
                    $printMDatas["PRINT_DATA"]["TOTAL_CNT"] += $printData["TOTAL_CNT"];
                }
                $printLDatas["M_MAP"][$setMcd] = $printMDatas;
            }

            $befLcd = $lCd;
            $befGrpLcd = $lGrpCd;
        }
        $result->free();

        if (is_array($printLDatas)) {
            $retList[] = $printLDatas;
        }

        return $retList;
    }

    function getLcd($lcdMap) {
        $stb = "";
        $sep = "";
        foreach ($lcdMap as $key => $lcd) {
            $stb .= $sep.$lcd;
            $sep = ".";
        }

        return $stb;
    }

    function getMcd($mcdMap) {
        $stb = "";
        $befMcd = 0;
        $karaMoji = "～";
        $karaFlg = false;
        $setData = "";
        asort($mcdMap);
        foreach ($mcdMap as $key => $mcd) {
            if ($befMcd == 0) {
                $stb .= "(".$mcd;
            } else {
                if (($befMcd + 1) == $mcd) {
                    $karaFlg = true;
                    $setData = $mcd;
                } else {
                    $setMcd = ".".$mcd;
                    $stb .= karaFlg ? $karaMoji.$befMcd.$setMcd : $setMcd;
                    $karaFlg = false;
                }
            }
            $befMcd = $mcd;
        }
        if ($karaFlg) {
            $stb .= $karaMoji.$setData;
        }
        $stb .= ")";
        return $stb;
    }

    function getMcd2($mcdMap) {
        $stb = "";
        $befMcd = 0;
        $karaMoji = "～";
        $karaFlg = false;
        $setData = "";
        asort($mcdMap);
        foreach ($mcdMap as $key => $mcd) {
            if ($befMcd == 0) {
                $stb .= $mcd;
            } else {
                if (($befMcd + 1) == $mcd) {
                    $karaFlg = true;
                    $setData = $mcd;
                } else {
                    $setMcd = ".".$mcd;
                    $stb .= karaFlg ? $karaMoji.$befMcd.$setMcd : $setMcd;
                    $karaFlg = false;
                }
            }
            $befMcd = $mcd;
        }
        if ($karaFlg) {
            $stb .= $karaMoji.$setData;
        }
        return $stb;
    }

    function getPrintKoumoku() {
        $retMap = array();

        $retMap["A"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "TITLE" => "Ａ　専門的・技術的職業",
                             "TITLE2" => "全　数",
                             "SUB_TITLE" => "　　　　　　　（０１～２０）",
                             "SUB_TITLE2" => "うち県内");
        $retMap["B"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "TITLE" => "Ｂ　管理的職業",
                             "TITLE2" => "全　数",
                             "SUB_TITLE" => "　　　　　　　（２１～２４）",
                             "SUB_TITLE2" => "うち県内");
        $retMap["C"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "TITLE" => "Ｃ　事務的職業",
                             "TITLE2" => "全　数",
                             "SUB_TITLE" => "　　　　　　　（２５～３１）",
                             "SUB_TITLE2" => "うち県内");
        $retMap["D"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "TITLE" => "Ｄ　販売の職業",
                             "TITLE2" => "全　数",
                             "SUB_TITLE" => "　　　　　　　（３２～３３）",
                             "SUB_TITLE2" => "うち県内");
        $retMap["E"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "TITLE" => "Ｅ　サービスの職業",
                             "TITLE2" => "全　数",
                             "SUB_TITLE" => "　　　　　　　（３４～３９）",
                             "SUB_TITLE2" => "うち県内");
        $retMap["F"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "TITLE" => "Ｆ　保安の職業",
                             "TITLE2" => "全　数",
                             "SUB_TITLE" => "　　　　　　　（４０～４２）",
                             "SUB_TITLE2" => "うち県内");
        $retMap["G"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "TITLE" => "Ｇ　農林漁業の職業",
                             "TITLE2" => "全　数",
                             "SUB_TITLE" => "　　　　　　　（４３～４５）",
                             "SUB_TITLE2" => "うち県内");
        $retMap["H"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "TITLE" => "Ｈ　運輸・通信の職業",
                             "TITLE2" => "全　数",
                             "SUB_TITLE" => "　　　　　　　（４６～５０）",
                             "SUB_TITLE2" => "うち県内");
        $retMap["I"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                             "TITLE" => "Ｉ　生産工程・労務の職業",
                             "TITLE2" => "全　数",
                             "SUB_TITLE" => "　　　　　　　（５１～８０）",
                             "SUB_TITLE2" => "うち県内");
        $retMap["I1"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                              "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                              "TITLE" => "　　製造・製作の職業",
                              "TITLE2" => "全　数",
                              "SUB_TITLE" => "　　　　　　　（５１～７２）",
                              "SUB_TITLE2" => "うち県内");
        $retMap["I2"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                              "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                              "TITLE" => "　　定置機関・建設機械運転電気作業",
                              "TITLE2" => "全　数",
                              "SUB_TITLE" => "　　　　　　　（７３、７４）",
                              "SUB_TITLE2" => "うち県内");
        $retMap["I3"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                              "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                              "TITLE" => "　　採掘・建設・労務の職業",
                              "TITLE2" => "全　数",
                              "SUB_TITLE" => "　　　　　　　（７５～８０）",
                              "SUB_TITLE2" => "うち県内");
        $retMap["TOTAL"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                                 "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                                 "TITLE" => "　　　　合計",
                                 "TITLE2" => "全　数",
                                 "SUB_TITLE" => "",
                                 "SUB_TITLE2" => "うち県内");
        return $retMap;
    }

    function getPrintKibo() {
        $retMap = array();
        $retMap["29"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                              "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                              "TITLE" => "２９人以下");
        $retMap["99"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                              "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                              "TITLE" => "３０　～９９人");
        $retMap["299"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                               "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                               "TITLE" => "１００　～　２９９人");
        $retMap["499"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                               "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                               "TITLE" => "３００　～　４９９人");
        $retMap["999"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                               "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                               "TITLE" => "５００　～　９９９人");
        $retMap["1000"] = array("L_ALL_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                                "L_IN_PRINTDATA" => array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0),
                                "TITLE" => "１，０００人以上");
        return $retMap;
    }

    function getGassan() {
        $retMap = array();
        $retMap["I1"] = "I";
        $retMap["I2"] = "I";
        $retMap["I3"] = "I";
        return $retMap;
    }

    function setLData($printLDatas, $printData, $prefCd) {
        if (is_array($printData)) {
            $printLDatas["L_ALL_PRINTDATA"]["MAN_CNT"] += $printData["MAN_CNT"];
            $printLDatas["L_ALL_PRINTDATA"]["WOMAN_CNT"] += $printData["WOMAN_CNT"];
            $printLDatas["L_ALL_PRINTDATA"]["TOTAL_CNT"] += $printData["TOTAL_CNT"];
            if ($prefCd == $this->schoolMst["PREF_CD"]) {
                $printLDatas["L_IN_PRINTDATA"]["MAN_CNT"] += $printData["MAN_CNT"];
                $printLDatas["L_IN_PRINTDATA"]["WOMAN_CNT"] += $printData["WOMAN_CNT"];
                $printLDatas["L_IN_PRINTDATA"]["TOTAL_CNT"] += $printData["TOTAL_CNT"];
            }
        }
        return $printLDatas;
    }

    function printOut($db, $printStudents, $grade, $majorCd) {
        $daigakuHope = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $daigakuAft = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $tandaiHope = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $tandaiAft = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $senmonHope = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $senmonAft = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $singakuSonotaHope = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $singakuSonotaAft = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $singakuKeiHope = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $singakuKeiAft = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);

        $kenNaiHope = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $kenNaiAft = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $kenNaiKoumuHope = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $kenNaiKoumuAft = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $kenGaiHope = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $kenGaiAft = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $kenGaiKoumuHope = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $kenGaiKoumuAft = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $shushokuSonotaHope = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $shushokuSonotaAft = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $shushokuKeiHope = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $shushokuKeiAft = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);

        $sonotaHope = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $sonotaAft = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $souGouHope = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        $souGouAft = array("MAN_CNT" => 0, "WOMAN_CNT" => 0, "TOTAL_CNT" => 0);
        foreach ($printStudents as $key => $student) {
            $souGouHope = $this->setCnt($souGouHope, $student);
            // 進学希望
            if ($student["IS_SINGAKU_HOPE"] || ($student["IS_SINGAKU"] && !$student["IS_SHUSYOKU"])) {
                $singakuKeiHope = $this->setCnt($singakuKeiHope, $student);
                if ($student["IS_SINGAKU"]) {
                    $souGouAft = $this->setCnt($souGouAft, $student);
                    $singakuKeiAft = $this->setCnt($singakuKeiAft, $student);
                    if ($student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "01" ||
                        $student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "02" ||
                        $student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "03"
                    ) {
                        $daigakuAft = $this->setCnt($daigakuAft, $student);
                    } else if ($student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "04" ||
                               $student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "05"
                    ) {
                        $tandaiAft = $this->setCnt($tandaiAft, $student);
                    } else if ($student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "06" ||
                               $student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "07"
                    ) {
                        $senmonAft = $this->setCnt($senmonAft, $student);
                    } else if ($student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "08" ||
                               $student["AFT_GRAD_COURSE_DAT_SIN"]["SCHOOL_GROUP"] == "09"
                    ) {
                        $singakuSonotaAft = $this->setCnt($singakuSonotaAft, $student);
                    } else {
                        $singakuSonotaAft = $this->setCnt($singakuSonotaAft, $student);
                    }
                }
                if (!$student["COURSE_HOPE_DAT"]["COURSE_KIND"]) {
                    $singakuSonotaHope = $this->setCnt($singakuSonotaHope, $student);
                } else if ($student["COURSE_HOPE_DAT"]["SCHOOL_GROUP1"] == "01" ||
                           $student["COURSE_HOPE_DAT"]["SCHOOL_GROUP1"] == "02" ||
                           $student["COURSE_HOPE_DAT"]["SCHOOL_GROUP1"] == "03"
                ) {
                    $daigakuHope = $this->setCnt($daigakuHope, $student);
                } else if ($student["COURSE_HOPE_DAT"]["SCHOOL_GROUP1"] == "04" ||
                           $student["COURSE_HOPE_DAT"]["SCHOOL_GROUP1"] == "05"
                ) {
                    $tandaiHope = $this->setCnt($tandaiHope, $student);
                } else if ($student["COURSE_HOPE_DAT"]["SCHOOL_GROUP1"] == "06" ||
                           $student["COURSE_HOPE_DAT"]["SCHOOL_GROUP1"] == "07"
                    ) {
                        $senmonHope = $this->setCnt($senmonHope, $student);
                } else if ($student["COURSE_HOPE_DAT"]["SCHOOL_GROUP1"] == "08" ||
                           $student["COURSE_HOPE_DAT"]["SCHOOL_GROUP1"] == "09"
                ) {
                    $singakuSonotaHope = $this->setCnt($singakuSonotaHope, $student);
                } else {
                    $singakuSonotaHope = $this->setCnt($singakuSonotaHope, $student);
                }

            } else {
                // 就職希望：無 or 未
                if (!$student["COURSE_HOPE_DAT"]["COURSE_KIND"] || $student["IS_MITEI_HOPE"]) {
                    $sonotaHope = $this->setCnt($sonotaHope, $student);
                } else {
                    $shushokuKeiHope = $this->setCnt($shushokuKeiHope, $student);
                    // 就職希望：家事手伝い
                    if ($student["IS_KAJI_HOPE"]) {
                        $sonotaHope = $this->setCnt($sonotaHope, $student);
                    } else {
                        if ($student["COURSE_HOPE_DAT"]["WORK_AREA1"] == "1") {
                            $kenNaiHope = $this->setCnt($kenNaiHope, $student);
                            // 公務員
                            if ($student["COURSE_HOPE_DAT"]["INTRODUCTION_DIV1"] == "3") {
                                $kenNaiKoumuHope = $this->setCnt($kenNaiKoumuHope, $student);
                            }
                        } else {
                            $kenGaiHope = $this->setCnt($kenGaiHope, $student);
                            // 公務員
                            if ($student["COURSE_HOPE_DAT"]["INTRODUCTION_DIV1"] == "3") {
                                $kenGaiKoumuHope = $this->setCnt($kenGaiKoumuHope, $student);
                            }
                        }
                    }
                }
                // 就職内定(確定者)
                if ($student["IS_SHUSYOKU"]) {
                    $souGouAft = $this->setCnt($souGouAft, $student);
                    $shushokuKeiAft = $this->setCnt($shushokuKeiAft, $student);
                    if ($student["AFT_GRAD_COURSE_DAT_SHU"]["PREF_CD"] == $this->schoolMst["PREF_CD"]) {
                        $kenNaiAft = $this->setCnt($kenNaiAft, $student);
                        // 公務員
                        if ($student["AFT_GRAD_COURSE_DAT_SHU"]["INTRODUCTION_DIV"] == "3") {
                            $kenNaiKoumuAft = $this->setCnt($kenNaiKoumuAft, $student);
                        }
                    } else {
                        $kenGaiAft = $this->setCnt($kenGaiAft, $student);
                        // 公務員
                        if ($student["AFT_GRAD_COURSE_DAT_SHU"]["INTRODUCTION_DIV"] == "3") {
                            $kenGaiKoumuAft = $this->setCnt($kenGaiKoumuAft, $student);
                        }
                    }
                }
            }
        }

        $majorName = $majorCd == "9999" ? "全学科" : $db->getOne(knje380Query::getMajorName($majorCd));

        $year = CTRL_YEAR;
        $month = "04";
        $day = "01";
        $grade_name = $db->getOne(knje380Query::getGrade(this, $grade));
        $retContents  = ",".common::Calc_Wareki($year, $month, $day)."度".$grade_name."進路希望調査\n";
        $retContents .= ",,,".common::DateConv1(str_replace("-", "/", CTRL_DATE), 0)."　現在\n\n\n";
        $retContents .= ",,,学科：　".$majorName."\n";
        $retContents .= ",,男,女,計\n";

        $percentTotal = 0;

        $percent = $db->getOne(knje380Query::getPercentSql($daigakuHope["TOTAL_CNT"], $souGouHope["TOTAL_CNT"]));
        $retContents .= "進学,大学,".$daigakuHope["MAN_CNT"]."　(".$daigakuAft["MAN_CNT"]."),".$daigakuHope["WOMAN_CNT"]."　(".$daigakuAft["WOMAN_CNT"]."),".$daigakuHope["TOTAL_CNT"]."　(".$daigakuAft["TOTAL_CNT"]."),".$percent."%\n";

        $percent = $db->getOne(knje380Query::getPercentSql($tandaiHope["TOTAL_CNT"], $souGouHope["TOTAL_CNT"]));
        $retContents .= ",短大,".$tandaiHope["MAN_CNT"]."　(".$tandaiAft["MAN_CNT"]."),".$tandaiHope["WOMAN_CNT"]."　(".$tandaiAft["WOMAN_CNT"]."),".$tandaiHope["TOTAL_CNT"]."　(".$tandaiAft["TOTAL_CNT"]."),".$percent."%\n";

        $percent = $db->getOne(knje380Query::getPercentSql($senmonHope["TOTAL_CNT"], $souGouHope["TOTAL_CNT"]));
        $retContents .= ",専門学校,".$senmonHope["MAN_CNT"]."　(".$senmonAft["MAN_CNT"]."),".$senmonHope["WOMAN_CNT"]."　(".$senmonAft["WOMAN_CNT"]."),".$senmonHope["TOTAL_CNT"]."　(".$senmonAft["TOTAL_CNT"]."),".$percent."%\n";

        $percent = $db->getOne(knje380Query::getPercentSql($singakuSonotaHope["TOTAL_CNT"], $souGouHope["TOTAL_CNT"]));
        $retContents .= ",その他,".$singakuSonotaHope["MAN_CNT"]."　(".$singakuSonotaAft["MAN_CNT"]."),".$singakuSonotaHope["WOMAN_CNT"]."　(".$singakuSonotaAft["WOMAN_CNT"]."),".$singakuSonotaHope["TOTAL_CNT"]."　(".$singakuSonotaAft["TOTAL_CNT"]."),".$percent."%\n";

        $percent = $db->getOne(knje380Query::getPercentSql($singakuKeiHope["TOTAL_CNT"], $souGouHope["TOTAL_CNT"]));
        $percentTotal += (int)$percent;
        $retContents .= ",合計,".$singakuKeiHope["MAN_CNT"]."　(".$singakuKeiAft["MAN_CNT"]."),".$singakuKeiHope["WOMAN_CNT"]."　(".$singakuKeiAft["WOMAN_CNT"]."),".$singakuKeiHope["TOTAL_CNT"]."　(".$singakuKeiAft["TOTAL_CNT"]."),".$percent."%\n";

        $percent = $db->getOne(knje380Query::getPercentSql($kenNaiHope["TOTAL_CNT"], $souGouHope["TOTAL_CNT"]));
        $retContents .= "就職,県内,".$kenNaiHope["MAN_CNT"]."　(".$kenNaiAft["MAN_CNT"]."),".$kenNaiHope["WOMAN_CNT"]."　(".$kenNaiAft["WOMAN_CNT"]."),".$kenNaiHope["TOTAL_CNT"]."　(".$kenNaiAft["TOTAL_CNT"]."),".$percent."%\n";

        $percent = $db->getOne(knje380Query::getPercentSql($kenNaiKoumuHope["TOTAL_CNT"], $souGouHope["TOTAL_CNT"]));
        $retContents .= ",　　県内公務員,".$kenNaiKoumuHope["MAN_CNT"]."　(".$kenNaiKoumuAft["MAN_CNT"]."),".$kenNaiKoumuHope["WOMAN_CNT"]."　(".$kenNaiKoumuAft["WOMAN_CNT"]."),".$kenNaiKoumuHope["TOTAL_CNT"]."　(".$kenNaiKoumuAft["TOTAL_CNT"]."),".$percent."%\n";

        $percent = $db->getOne(knje380Query::getPercentSql($kenGaiHope["TOTAL_CNT"], $souGouHope["TOTAL_CNT"]));
        $retContents .= ",県外,".$kenGaiHope["MAN_CNT"]."　(".$kenGaiAft["MAN_CNT"]."),".$kenGaiHope["WOMAN_CNT"]."　(".$kenGaiAft["WOMAN_CNT"]."),".$kenGaiHope["TOTAL_CNT"]."　(".$kenGaiAft["TOTAL_CNT"]."),".$percent."%\n";

        $percent = $db->getOne(knje380Query::getPercentSql($kenGaiKoumuHope["TOTAL_CNT"], $souGouHope["TOTAL_CNT"]));
        $retContents .= ",　　県外公務員,".$kenGaiKoumuHope["MAN_CNT"]."　(".$kenGaiKoumuAft["MAN_CNT"]."),".$kenGaiKoumuHope["WOMAN_CNT"]."　(".$kenGaiKoumuAft["WOMAN_CNT"]."),".$kenGaiKoumuHope["TOTAL_CNT"]."　(".$kenGaiKoumuAft["TOTAL_CNT"]."),".$percent."%\n";

        $percent = $db->getOne(knje380Query::getPercentSql($shushokuSonotaHope["TOTAL_CNT"], $souGouHope["TOTAL_CNT"]));
        $retContents .= ",その他,".$shushokuSonotaHope["MAN_CNT"]."　(".$shushokuSonotaAft["MAN_CNT"]."),".$shushokuSonotaHope["WOMAN_CNT"]."　(".$shushokuSonotaAft["WOMAN_CNT"]."),".$shushokuSonotaHope["TOTAL_CNT"]."　(".$shushokuSonotaAft["TOTAL_CNT"]."),".$percent."%\n";

        $percent = $db->getOne(knje380Query::getPercentSql($shushokuKeiHope["TOTAL_CNT"], $souGouHope["TOTAL_CNT"]));
        $percentTotal += (int)$percent;
        $retContents .= ",合計,".$shushokuKeiHope["MAN_CNT"]."　(".$shushokuKeiAft["MAN_CNT"]."),".$shushokuKeiHope["WOMAN_CNT"]."　(".$shushokuKeiAft["WOMAN_CNT"]."),".$shushokuKeiHope["TOTAL_CNT"]."　(".$shushokuKeiAft["TOTAL_CNT"]."),".$percent."%\n";

        $percent = 100 - $percentTotal;
        $retContents .= ",その他,".$sonotaHope["MAN_CNT"]."　(".$sonotaAft["MAN_CNT"]."),".$sonotaHope["WOMAN_CNT"]."　(".$sonotaAft["WOMAN_CNT"]."),".$sonotaHope["TOTAL_CNT"]."　(".$sonotaAft["TOTAL_CNT"]."),".$percent."%\n";

        $percent = $db->getOne(knje380Query::getPercentSql($souGouHope["TOTAL_CNT"], $souGouHope["TOTAL_CNT"]));
        $retContents .= ",総合計,".$souGouHope["MAN_CNT"]."　(".$souGouAft["MAN_CNT"]."),".$souGouHope["WOMAN_CNT"]."　(".$souGouAft["WOMAN_CNT"]."),".$souGouHope["TOTAL_CNT"]."　(".$souGouAft["TOTAL_CNT"]."),".$percent."%\n";

        $retContents .= ",,,,()内は内定・合格者数\n\n\n";

        return $retContents;
    }
}
?>
