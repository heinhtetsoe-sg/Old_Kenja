<?php

require_once('for_php7.php');

class knjd123xModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $selectdata;
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knjd123xModel() {
        common::GetControlMaster_Fnc($this->control);
    }
    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("YEAR"   =>  VARS::post("YEAR"),   //年度
                             "GRADE"  =>  VARS::post("GRADE")   //学年
                            );

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        $this->virus     = ($this->Properties["useVirus"] == 'true') ? true : false;
        $this->koudome   = ($this->Properties["useKoudome"] == 'true') ? true : false;
        $this->selectdata = VARS::post("selectdata");
    }

    function getDownloadModel() {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        //DB接続
        $db = Query::dbCheckOut();
        //ヘッダ配列
        //教育課程対応
        if ($this->Properties["useCurriculumcd"] == '1') {
            $header = array("学年",
                            "組",
                            "出席番号",
                            "学籍番号",
                            "氏名",
                            "教科コード",
                            "学校校種",
                            "教育課程コード",
                            "科目コード",
                            "科目名",
                            "講座コード",
                            "講座名",
                            "前期_中間評価",
                            "前期_評価",
                            "後期_中間評価",
                            "後期_評価",
                            "学年評定",
                            "履修単位数",
                            "修得単位",
                            "単位数",
                            "前期_時数",
                            "前期_遅刻早退",
                            "前期_欠時数",
                            "前期_欠課数",
                            "後期_時数",
                            "後期_遅刻早退",
                            "後期_欠時数",
                            "後期_欠課数",
                            "備考"
                            );
        } else {
            $header = array("学年",
                            "組",
                            "出席番号",
                            "学籍番号",
                            "氏名",
                            "科目コード",
                            "科目名",
                            "講座コード",
                            "講座名",
                            "前期_中間評価",
                            "前期_評価",
                            "後期_中間評価",
                            "後期_評価",
                            "学年評定",
                            "履修単位数",
                            "修得単位",
                            "単位数",
                            "前期_時数",
                            "前期_遅刻早退",
                            "前期_欠時数",
                            "前期_欠課数",
                            "後期_時数",
                            "後期_遅刻早退",
                            "後期_欠時数",
                            "後期_欠課数",
                            "備考"
                            );
        }
        //ヘッダ配列を出力変数にセット
        $headerContents = implode($header, $comma) .$lf;

        $query = knjd123xQuery::getSchoolMst();
        $school_mst = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $dataContents = "";
        $grade_hr_class_array = explode(',', $this->selectdata);
        foreach ($grade_hr_class_array as $grade_hr_class) {
            $query = knjd123xQuery::getCsvQuery($grade_hr_class, $this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dataContents .= $row["GRADE"]           . ",";
                $dataContents .= $row["HR_CLASS"]        . ",";
                $dataContents .= $row["ATTENDNO"]        . ",";
                $dataContents .= $row["SCHREGNO"]        . ",";
                $dataContents .= $row["NAME_SHOW"]       . ",";
                //教育課程対応
                if ($this->Properties["useCurriculumcd"] == '1') {
                    $dataContents .= $row["CLASSCD"]      . ",";
                    $dataContents .= $row["SCHOOL_KIND"]      . ",";
                    $dataContents .= $row["CURRICULUM_CD"]      . ",";
                }
                $dataContents .= $row["SUBCLASSCD"]      . ",";
                $dataContents .= $row["SUBCLASSNAME"]    . ",";
                $dataContents .= $row["CHAIRCD"]         . ",";
                $dataContents .= $row["CHAIRNAME"]       . ",";
                //*_DI のフィールドに値があればそっちを使う
                if ($row["SEM1_INTR_VALUE_DI"]) {
                    $dataContents .= $row["SEM1_INTR_VALUE_DI"] . ",";
                } else {
                    $dataContents .= $row["SEM1_INTR_VALUE"] . ",";
                }
                if ($row["SEM1_VALUE_DI"]) {
                    $dataContents .= $row["SEM1_VALUE_DI"] . ",";
                } else {
                    $dataContents .= $row["SEM1_VALUE"] . ",";
                }
                if ($row["SEM2_INTR_VALUE_DI"]) {
                    $dataContents .= $row["SEM2_INTR_VALUE_DI"] . ",";
                } else {
                    $dataContents .= $row["SEM2_INTR_VALUE"] . ",";
                }
                if ($row["SEM2_VALUE_DI"]) {
                    $dataContents .= $row["SEM2_VALUE_DI"] . ",";
                } else {
                    $dataContents .= $row["SEM2_VALUE"] . ",";
                }
                if ($row["GRAD_VALUE_DI"]) {
                    $dataContents .= $row["GRAD_VALUE_DI"] . ",";
                } else {
                    $dataContents .= $row["GRAD_VALUE"] . ",";
                }
                $dataContents .= $row["COMP_CREDIT"]     . ",";
                $dataContents .= $row["GET_CREDIT"]      . ",";
                $dataContents .= $row["CREDITS"]         . ",";

                $late_early_1 = $row["LATE_1"] + $row["EARLY_1"];
                $ketu_jisu_1  = $row["SICK_1"] + $row["NOTICE_1"] + $row["NONOTICE_1"] + $row["NURSEOFF_1"];
                if ($school_mst["SUB_OFFDAYS"]  == "1")  $ketu_jisu_1 += $row["OFFDAYS_1"];
                if ($school_mst["SUB_ABSENT"]   == "1")  $ketu_jisu_1 += $row["ABSENT_1"];
                if ($school_mst["SUB_SUSPEND"]  == "1")  $ketu_jisu_1 += $row["SUSPEND_1"];
                if ($school_mst["SUB_MOURNING"] == "1")  $ketu_jisu_1 += $row["MOURNING_1"];
                if ($school_mst["SUB_VIRUS"] == "1")     $ketu_jisu_1 += $row["VIRUS_1"];
                if ($school_mst["SUB_KOUDOME"] == "1")   $ketu_jisu_1 += $row["KOUDOME_1"];

                if ($school_mst["ABSENT_COV"] == '1' || $school_mst["ABSENT_COV"] == '3') {
                    if ($school_mst["ABSENT_COV"] == '1') {
                        $late_early = $late_early_1 - $late_early_1 % $school_mst["ABSENT_COV_LATE"]; //余りが出ないようにする
                        $kekka_jisuu_1 = $ketu_jisu_1 + $late_early / $school_mst["ABSENT_COV_LATE"];
                    } else {
                        $late_early = $late_early_1;
                        $kekka_jisuu_1 = $ketu_jisu_1 + $late_early / $school_mst["ABSENT_COV_LATE"];
                        $kekka_jisuu_1 = round($kekka_jisuu_1, 1); //四捨五入
                    }
                } else {
                    $kekka_jisuu_1 = $ketu_jisu_1;
                }

                $dataContents .= $row["LESSON_1"] . ",";
                $dataContents .= $late_early_1    . ",";
                $dataContents .= $ketu_jisu_1     . ",";
                $dataContents .= $kekka_jisuu_1   . ",";

                $late_early_2 = $row["LATE_2"] + $row["EARLY_2"];
                $ketu_jisu_2  = $row["SICK_2"] + $row["NOTICE_2"] + $row["NONOTICE_2"] + $row["NURSEOFF_2"];
                if ($school_mst["SUB_OFFDAYS"]  == "1")  $ketu_jisu_2 += $row["OFFDAYS_2"];
                if ($school_mst["SUB_ABSENT"]   == "1")  $ketu_jisu_2 += $row["ABSENT_2"];
                if ($school_mst["SUB_SUSPEND"]  == "1")  $ketu_jisu_2 += $row["SUSPEND_2"];
                if ($school_mst["SUB_MOURNING"] == "1")  $ketu_jisu_2 += $row["MOURNING_2"];
                if ($school_mst["SUB_VIRUS"] == "1")     $ketu_jisu_2 += $row["VIRUS_2"];
                if ($school_mst["SUB_KOUDOME"] == "1")   $ketu_jisu_2 += $row["KOUDOME_2"];

                if ($school_mst["ABSENT_COV"] == '1' || $school_mst["ABSENT_COV"] == '3') {
                    if ($school_mst["ABSENT_COV"] == '1') {
                        $late_early = $late_early_2 - $late_early_2 % $school_mst["ABSENT_COV_LATE"]; //余りが出ないようにする
                        $kekka_jisuu_2 = $ketu_jisu_2 + $late_early / $school_mst["ABSENT_COV_LATE"];
                    } else {
                        $late_early = $late_early_2;
                        $kekka_jisuu_2 = $ketu_jisu_2 + $late_early / $school_mst["ABSENT_COV_LATE"];
                        $kekka_jisuu_2 = round($kekka_jisuu_2, 1); //四捨五入
                    }
                } else {
                    $kekka_jisuu_2 = $ketu_jisu_2;
                }

                $dataContents .= $row["LESSON_2"] . ",";
                $dataContents .= $late_early_2    . ",";
                $dataContents .= $ketu_jisu_2     . ",";
                $dataContents .= $kekka_jisuu_2   . ",";

                $dataContents .= $row["REMARK"];
                $dataContents .= $lf;

                $data = true;
            }
        }

        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        $contents  = "";
        $contents .= $headerContents;
        $contents .= $dataContents;
        if ($data) {
            $filename = sprintf("%s.csv", CTRL_YEAR . "年度 成績一覧表");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>
