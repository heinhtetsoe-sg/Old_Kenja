<?php

require_once('for_php7.php');

class knja122Model extends Model {
    var $cmd;
    var $schregno = "";
    var $grade = "";
    var $name = "";
    var $field    = array();
    var $control;
    var $exp_year;       //左フレームから送られてくる年度
    var $exp_semester;   //左フレームから送られてくる学期

    function knja122Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }

        //メイン画面より送られてくるパラメーター
        $this->field = array("TOTALSTUDYACT"    => VARS::post("TOTALSTUDYACT"),
                             "VIEWREMARK"       => VARS::post("VIEWREMARK"),
                             "TOTALSTUDYVAL"    => VARS::post("TOTALSTUDYVAL"),
                             "TOTALREMARK"      => VARS::post("TOTALREMARK"),
                             "ATTENDREC_REMARK" => VARS::post("ATTENDREC_REMARK"),
                             "UPDATED"          => VARS::post("UPDATED")
                             );

        //subform1より送られてくるパラメーター
        $this->field1 = array("SEIKATU"  => VARS::post("SEIKATU"),
                              "KENKO"    => VARS::post("KENKO"),
                              "JISYU"    => VARS::post("JISYU"),
                              "SEKININ"  => VARS::post("SEKININ"),
                              "SOUI"     => VARS::post("SOUI"),
                              "OMOIYARI" => VARS::post("OMOIYARI"),
                              "SEIMEI"   => VARS::post("SEIMEI"),
                              "KINROU"   => VARS::post("KINROU"),
                              "KOUSEI"   => VARS::post("KOUSEI"),
                              "KOUKYOU"  => VARS::post("KOUKYOU")
                              );

        //subform2より送られてくるパラメーター
        $this->field2 = array("CLASSACT"    => VARS::post("CLASSACT"),
                              "STUDENTACT"  => VARS::post("STUDENTACT"),
                              "CLUBACT"     => VARS::post("CLUBACT"),
                              "SCHOOLEVENT" => VARS::post("SCHOOLEVENT")
                              );

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("GRADE")!="") {
            $this->grade      = VARS::get("GRADE");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }

        //左フレームから送られてくる年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //左フレームから送られてくる学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }
    }

    /**************************************************************************************/
    /********************************* 入力チェック ***************************************/
    /**************************************************************************************/
    //メイン画面の入力項目チェック
    function validateInput() {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->field["TOTALSTUDYACT"]) > 226) {
            $this->setWarning("MSG915","(学習･活動は226バイトまでです)");
            return false;
        }
        if (strlen($this->field["TOTALSTUDYVAL"]) > 460) {
            $this->setWarning("MSG915","(評価は460バイトまでです)");
            return false;
        }
        if (strlen($this->field["TOTALREMARK"]) > 1090) {
            $this->setWarning("MSG915","(総合所見は1090バイトまでです)");
            return false;
        }
        if (strlen($this->field["ATTENDREC_REMARK"]) > 122) {
            $this->setWarning("MSG915","(出欠の記録備考は122バイトまでです)");
            return false;
        }
        if (strlen($this->field["VIEWREMARK"]) > 226) {
            $this->setWarning("MSG915","(観点は226バイトまでです)");
            return false;
        }
        return true;
    }

    //subform2の入力項目チェック
    function validateInput2() {
        if ($this->schregno=="") {
            $this->setWarning("MSG304");
            return false;
        }
        if (strlen($this->field2["CLASSACT"]) > 218) {
            $this->setWarning("MSG915","(学級活動は218バイトまでです)");
            return false;
        }
        if (strlen($this->field2["STUDENTACT"]) > 218) {
            $this->setWarning("MSG915","(生徒会活動は218バイトまでです)");
            return false;
        }
        if (strlen($this->field2["CLUBACT"]) > 218) {
            $this->setWarning("MSG915","(クラブ活動は218バイトまでです)");
            return false;
        }
        if (strlen($this->field2["SCHOOLEVENT"]) > 218) {
            $this->setWarning("MSG915","(学校行事は218バイトまでです)");
            return false;
        }
        return true;
    }

    /******************************************************************************/
    /********************************* 更新 ***************************************/
    /******************************************************************************/
    //メイン画面の更新
    function &getUpdateModel() {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }

    //subform1の更新
    function &getUpdateModel1() {
        $this->update1();
        $this->setMessage("MSG201");
    }

    //subform2の更新
    function &getUpdateModel2() {
        if (!$this->validateInput2()) {
            return;
        }
        $this->update2();
        $this->setMessage("MSG201");
    }

    /**********************************************************************************/
    /********************************* DBの更新 ***************************************/
    /**********************************************************************************/
    //メイン画面の更新
    function &update() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knja122Query::getAnnual($this); //ANNUAL取得
        $annual = $db->getOne($query);

        $htrainDat  = knja122Query::getTrainRow($this, $db);  //レコードが存在するかチェック

        if (is_array($htrainDat)) {
            knja122Query::getUpdateHtrainDat($this, $annual, $db); //レコードがあるときはアップデート
        } else {
            knja122Query::getInsertHtrainDat($this, $annual, $db); //レコードがないときはインサート
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return $result;
    }

    //subform1の更新
    function &update1() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knja122Query::getAnnual($this);
        $annual = $db->getOne($query);

        knja122Query::getDeleteBehavior($this->exp_year, $this->schregno, $db);
        knja122Query::getInsertBehavior($this, $annual, $db);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return $result;
    }

    //subform2の更新
    function &update2() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knja122Query::getAnnual($this); //ANNUAL取得
        $annual = $db->getOne($query);

        $htrainDat  = knja122Query::getTrainRow($this, $db);  //HTRAINREMARK_DAT  のレコードが存在するかチェック

        if (is_array($htrainDat)) {
            knja122Query::getUpdateHtrainDat2($this, $annual, $db); //レコードがあるときはアップデート
        } else {
            knja122Query::getInsertHtrainDat2($this, $annual, $db); //レコードがないときはインサート
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return $result;
    }
}
?>
