<?php

require_once('for_php7.php');

class knjd139sModel extends Model {

    var $cmd;
    var $exp_year;
    var $exp_semester;
    var $schregno;
    var $schoolKind;
    var $name;
    var $field = array();
    var $control;
    var $prjTitle = "";

    function knjd139sModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {

        if (VARS::get("PROGRAMID") != "") {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->exp_grade);
            unset($this->dispPatern);
        }

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),
                             "REMARK01_01"      =>  VARS::post("REMARK01_01"),
                             "REMARK01_02"      =>  VARS::post("REMARK01_02"),
                             "REMARK01_03"      =>  VARS::post("REMARK01_03"),
                             "REMARK02_02"      =>  VARS::post("REMARK02_02"),
                             "REMARK02_01"      =>  VARS::post("REMARK02_01"),
                             "REMARK02_03"      =>  VARS::post("REMARK02_03"),
                             "REMARK02_04"      =>  VARS::post("REMARK02_04"),
                             "ATTENDREC_REMARK" =>  VARS::post("ATTENDREC_REMARK"),
                             "COMMUNICATION"    =>  VARS::post("COMMUNICATION"),
                             "UPDATED"          =>  VARS::post("UPDATED")
                             );


        $this->cmd = VARS::request("cmd");

        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year     = VARS::get("EXP_YEAR");
            $this->exp_semester = VARS::get("EXP_SEMESTER");
            $this->exp_grade = VARS::get("GRADE");
        }
        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }
        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->schregno);
            unset($this->name);
            unset($this->exp_grade);
        }
        
        if (VARS::get("sendSEME") != "") {
            $this->ikkatsuSeme = VARS::get("sendSEME");
        }

        //表示パターンの設定。パターン１：1～4年、パターン２：1～4年以外
        $grade = $this->exp_grade * 1;
        $this->dispPatern = 1;
        if ($grade > 4) {
            $this->dispPatern = 2;
        }

        if ($this->dispPatern == 1) {
            $this->prjTitle = "パーソナル";
        } else {
            $this->prjTitle = "探究テーマ";
        }

        //一括更新用
        if ($this->cmd == "ikkatsu_update") {
            $this->ikkatsu_data["selectdata"] = VARS::post("selectdata");
            $this->ikkatsu_data["hreport"] = array();
            $this->ikkatsu_data["check1"] = array(VARS::post("CHECK_ALL1"),//ALL
                                                 VARS::post("CHECK1_1"),
                                                 VARS::post("CHECK1_2"),
                                                 VARS::post("CHECK1_3"),
                                                 VARS::post("CHECK1_4"),
                                                 );
            $this->ikkatsu_data["check2"] = array(VARS::post("CHECK_ALL2"),//ALL
                                                 VARS::post("CHECK2_1"),
                                                 );
        }
        if(VARS::get("ini2")) {
            unset($this->ikkatsu_data);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

    }

    //入力項目チェック
    function validateInput() {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }

        if ($this->cmd != "ikkatsu_update") {
            $moji = 25;
            $gyou = 1;
            if (strlen($this->field["REMARK01_01"]) > ($moji * 3)) {
                $ttl = 
                $this->setWarning("MSG915","({$this->prjTitle}は{$moji}文字までです)".strlen($this->field["REMARK01_01"]));
                return false;
            }
            $moji = 10;
            $gyou = 1;
            if (strlen($this->field["REMARK01_02"]) > ($moji * 3)) {
                $this->setWarning("MSG915","(体験学習テーマは{$moji}文字までです)");
                return false;
            }
            $moji = 10;
            $gyou = 1;
            if (strlen($this->field["REMARK01_03"]) > ($moji * 3)) {
                $this->setWarning("MSG915","(フィールドワークの成果は{$moji}文字までです)");
                return false;
            }
            $moji = 25;
            $gyou = 1;
            if (strlen($this->field["REMARK02_02"]) > ($moji * 3)) {
                $this->setWarning("MSG915","(異学年齢活動・表現は{$moji}文字までです)");
                return false;
            }
            $moji = 10;
            $gyou = 1;
            if (strlen($this->field["REMARK02_03"]) > ($moji * 3)) {
                $this->setWarning("MSG915","(学校行事{$moji}文字までです)");
                return false;
            }
            $moji = 10;
            $gyou = 3;
            if ($this->validate_row_cnt($this->field["REMARK02_01"], ($moji * 2)) > $gyou) {
                $this->setWarning("MSG915","(委員会活動・係活動は{$gyou}行までです)");
                return false;
            }
            if ($model->dispPatern == 2) {
                $moji = 10;
                $gyou = 1;
                if (strlen($this->field["REMARK02_04"]) > ($moji * 3)) {
                    $this->setWarning("MSG915","(その他は{$gyou}文字までです)");
                    return false;
                }
            }
            $moji = 25;
            $gyou = 1;
            if (strlen($this->field["ATTENDREC_REMARK"]) > ($moji * 3)) {
                $this->setWarning("MSG915","(出欠席の記録備考は{$moji}文字までです)");
                return false;
            }
            $moji = 18;
            $gyou = 7;
            if ($this->validate_row_cnt($this->field["COMMUNICATION"], ($moji * 2)) > $gyou) {
                $this->setWarning("MSG915","(所見は{$gyou}行までです)");
                return false;
            }
        } else {
            //最低ひとつでもチェックがついてる場合のみ更新
            if (!in_array("1", $this->ikkatsu_data["check1"]) && !in_array("1", $this->ikkatsu_data["check2"])) {
                $this->setCmd("ikkatsu");
                $this->setWarning("更新する項目がありません");
                return false;
            }
            //生徒が選択されてない場合
            if (!strlen($this->ikkatsu_data["selectdata"])) {
                $this->setCmd("ikkatsu");
                $this->setWarning("MSG304");
                return false;
            }
            if ($this->ikkatsu_data["check1"][1] == "1") {
                $moji = 25;
                $gyou = 1;
                if (strlen($this->field["REMARK01_01"]) > ($moji * 3)) {
                    $ttl = 
                    $this->setWarning("MSG915","({$this->prjTitle}は{$moji}文字までです)".strlen($this->field["REMARK01_01"]));
                    return false;
                }
            }
            if ($this->ikkatsu_data["check1"][2] == "1") {
                $moji = 10;
                $gyou = 1;
                if (strlen($this->field["REMARK01_02"]) > ($moji * 3)) {
                    $this->setWarning("MSG915","(体験学習テーマは{$moji}文字までです)");
                    return false;
                }
            }
            if ($this->ikkatsu_data["check1"][3] == "1") {
                $moji = 10;
                $gyou = 1;
                if (strlen($this->field["REMARK01_03"]) > ($moji * 3)) {
                    $this->setWarning("MSG915","(フィールドワークの成果は{$moji}文字までです)");
                    return false;
                }
            }
            if ($this->ikkatsu_data["check1"][4] == "1") {
                $moji = 25;
                $gyou = 1;
                if (strlen($this->field["REMARK02_02"]) > ($moji * 3)) {
                    $this->setWarning("MSG915","(異学年齢活動・表現は{$moji}文字までです)");
                    return false;
                }
            }
            if ($this->ikkatsu_data["check2"][1] == "1") {
                $moji = 10;
                $gyou = 1;
                if (strlen($this->field["REMARK02_03"]) > ($moji * 3)) {
                    $this->setWarning("MSG915","(学校行事は{$moji}文字までです)");
                    return false;
                }
            }
        }

        return true;
    }

    //更新用モデル
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        
        $this->update();
        $this->setMessage("MSG201");
    }

    //更新
    function &update() {
        knjd139sQuery::getUpdateQuery($this);
        return ;
    }

    function &IkkatsuModel() {
        if (!$this->validateInput()) {
            return;
        }

        knjd139sQuery::IkkatsuUpdate($db, $this);
        $this->setMessage("MSG201");
    }

}

?>
