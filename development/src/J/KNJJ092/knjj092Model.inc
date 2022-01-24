<?php

require_once('for_php7.php');

class knjj092Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    var $SelectCommittee; //左側で選んだ委員会
    var $committeeflg;    //左側でクラブが変更されたかどうか

    var $schregNo;      //キー１
    var $committeecd;     //キー２
    var $seq;             //キー２

    var $schregNo2;

    function knjj092Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->SelectCommittee);
            unset($this->GradeClass);
            unset($this->schregNo);
            unset($this->committeecd);
        }

        $this->field = array(
//                    /*-------          committee_history_dat テーブル用         --------------*/
                             "SCHREGNO"         =>  VARS::request("SCHREGNO"),      //在校生検索ボタンのプログラムからの学籍番号
                             "GRADE"            =>  VARS::request("GRADE"),         //学年
                             "COMMITTEECODE1"   =>  VARS::post("COMMITTEECODE1"),   //委員会コード
                             "HID_COMMITTEECD"  =>  VARS::post("HID_COMMITTEECD"),  //委員会コード（4桁）
                             "EXECUTIVECD"      =>  VARS::post("EXECUTIVECD"),      //役職区分
                             "UPDATED"          =>  VARS::post("UPDATED"),          //更新日付

                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //クラス
                             "COMMITTEE_IKKATSU"    =>  VARS::post("COMMITTEE_IKKATSU"),    //一括画面委員会コード
                             "SUB_SCHREGNO"         =>  VARS::post("selectdata"),           //学籍番号

                            );

        $this->cmd = VARS::request("cmd");

        //クラブ選択リスト
        if (VARS::request("COMMITTEE_YEAR_LIST")) {
            $this->SelectCommittee = VARS::request("COMMITTEE_YEAR_LIST");
        } else if (VARS::request("RELOADCOMMITTEE")) {
            $this->SelectCommittee = VARS::request("RELOADCOMMITTEE");
        }
        //更新後この行にスクロールバーを移動させる(左フレームのこと)ために使う変数(schregNo2)
        if (VARS::request("SCHREGNO")) {
            $this->schregNo2 = VARS::request("SCHREGNO");
        }

        //左フレームで委員会が変わったかどうかのフラグ
        if (VARS::request("COMMITTEEFLG")) {
            $this->committeeflg = VARS::request("COMMITTEEFLG");
        } else {
            $this->committeeflg = "";
        }

        //list→edit
        if (VARS::get("SCHREGNO")) {
            $this->schregNo = VARS::get("SCHREGNO");
        }
        if (VARS::get("COMMITTEECD")) {
            $this->committeecd = VARS::get("COMMITTEECD");
        }
        if (VARS::get("SEQ")) {
            $this->seq = VARS::get("SEQ");
        }
        if(VARS::request("SCHKIND") != ""){
            $this->schkind = VARS::request("SCHKIND");
        }
        if(VARS::request("SEMESTER") != ""){
            $this->semester = VARS::request("SEMESTER");
        }

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        } else if ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $this->schkind = SCHOOLKIND;
        }
    }

    function schreg_chk($schregno) {
        $row = knjj092Query::getSchregregd_data($this, CTRL_YEAR, CTRL_SEMESTER, $schregno);
        if (!isset($row)) {
            $this->setWarning("","該当する学籍番号はありません。");
            return false;
        } else {
            return true;
        }
    }

    //入力項目チェック
    function validateInput($cmd) {
        // 権限チェック
        if (common::SecurityCheck(STAFFCD, PROGRAMID) != DEF_UPDATABLE && common::SecurityCheck(STAFFCD, PROGRAMID) != DEF_UPDATE_RESTRICT) {
            $this->setWarning("MSG300");
            return false;
        }

        if ($cmd == "add") {
            //必須項目チェック
            if ($this->field["SCHREGNO"] == "" || $this->field["HID_COMMITTEECD"] == "") {
                $this->setWarning("MSG301");
                return false;
            }
        }

        if ($cmd == "update" || $cmd == "delete") {
            //リストより選択しているか
            if ($this->schregNo == "" || $this->committeecd == "") {
                $this->setWarning("MSG308");
                return false;
            }
            //必須項目が変更されていないか
            if ($this->field["SCHREGNO"] != $this->schregNo) {
                $this->setWarning("MSG308","(学籍番号/氏名)");
                return false;
            }
            if ($this->field["HID_COMMITTEECD"] != $this->committeecd) {
                $this->setWarning("MSG308","(委員会)");
                return false;
            }

            //記録備考データ存在チェック
            $cnt = knjj092Query::checkDetailExist($this);
            if ($cmd == "delete" && $cnt > 0){
                $this->setWarning("MSG919","（記録備考データ有）");
                return false;
            }
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        if ($this->validateInput("update")) {
            if ($this->update()) {
                $this->setMessage("MSG201");
                $this->schregNo    = $this->field["SCHREGNO"];
                $this->committeecd = $this->field["HID_COMMITTEECD"];
                return;
            }
        }
    }
    function &update() {
        $result = knjj092Query::getUpdateQuery($this, $this->field);
        return $result;
    }

    function &getInsertModel() {
        if ($this->validateInput("add")) {
            if ($this->insert()) {
                $this->setMessage("MSG201");
                $this->schregNo     = $this->field["SCHREGNO"];
                $this->committeecd  = $this->field["HID_COMMITTEECD"];
                return;
            }
        }
    }
    function &insert() {
        $result = knjj092Query::getInsertQuery($this, $this->field);
        return $result;
    }

    function &getDeleteModel() {
        //入力チェック
        if ($this->validateInput("delete")) {
            $this->delete();
            $this->setMessage("MSG201");
            return;
        }
    }
    function &delete() {
        $result = knjj092Query::getDeleteQuery($this, $this->field);
        return $result;
    }

/******** 一括更新処理  ********/
    //編集完了用のモデルを返します
    function &getSubInsertModel() {
        knjj092Query::getSubInsertQuery($this);

        $this->setMessage("MSG201");

        return;
    }
}
?>
