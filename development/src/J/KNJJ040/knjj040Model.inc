<?php

require_once('for_php7.php');

class knjj040Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    var $SelectClub; //左側で選んだ部クラブ
    var $clubflg;    //左側でクラブが変更されたかどうか

    var $gakusekino; //キー１
    var $clubcd;     //キー２
    var $enterdate;  //キー３

    var $gakusekino2;

    var $namecd1 = "J001";  //固定値（名称区分）

    function knjj040Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->SelectClub);
            unset($this->GradeClass);
            unset($this->gakusekino);
            unset($this->clubcd);
            unset($this->enterdate);
            unset($this->schkind);
        }

        $this->field = array(
//                    /*-------          club_history_dat テーブル用         --------------*/
                             "SCHREGNO"         =>  VARS::request("SCHREGNO"),      //在校生検索ボタンのプログラムからの学籍番号
                             "CLUBCODE1"        =>  VARS::post("CLUBCODE1"),        //部クラブコード
                             "CLUBCODEALL"      =>  VARS::post("CLUBCODEALL"),      //部クラブコード（4桁）
                             "SDATE"            =>  VARS::post("SDATE"),            //入部日付
                             "EDATE"            =>  VARS::post("EDATE"),            //退部日付
                             "EXECUTIVECD"      =>  VARS::post("EXECUTIVECD"),      //役職区分
                             "REMARK"           =>  VARS::post("REMARK"),           //備考
                             "UPDATED"          =>  VARS::post("UPDATED"),          //更新日付

                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //クラス
                             "SUB_SDATE"        =>  VARS::post("SUB_SDATE"),        //入部日付
                             "SUB_EDATE"        =>  VARS::post("SUB_EDATE"),        //退部日付
                             "SUB_SCHREGNO"     =>  VARS::post("selectdata"),       //学籍番号

                            );

        $this->cmd = VARS::request("cmd");

        //クラブ選択リスト
        if (VARS::request("CLUB_YEAR_LIST")) {
            $this->SelectClub = VARS::request("CLUB_YEAR_LIST");
        } else if (VARS::request("RELOADCLUB")) {
            $this->SelectClub = VARS::request("RELOADCLUB");
        }
        //更新後この行にスクロールバーを移動させる(左フレームのこと)ために使う変数(gakusekino2)
        if (VARS::request("GAKUSEKINO")) {
            $this->gakusekino2 = VARS::request("GAKUSEKINO");
        }

        //左フレームで部クラブが変わったかどうかのフラグ
        if (VARS::request("CLUBFLG")) {
            $this->clubflg = VARS::request("CLUBFLG");
        } else {
            $this->clubflg = "";
        }

        //list→edit
        if (VARS::get("GAKUSEKINO")) {
            $this->gakusekino = VARS::get("GAKUSEKINO");
        }
        if (VARS::get("CLUBCD")) {
            $this->clubcd = VARS::get("CLUBCD");
        }
        if (VARS::get("SDATE")) {
            $this->enterdate = VARS::get("SDATE");
        }
        if(VARS::request("SCHKIND") != ""){
            $this->schkind = VARS::request("SCHKIND");
        }

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //校種初期値（プログラムを開き、左画面は何も触らないで [追加] する際、
        //            $this->schkind がセットされない時があるため（原因不明））
        if ($this->Properties["useClubMultiSchoolKind"] != '1' && $this->Properties["use_prg_schoolkind"] == "1") {
            $db = Query::dbCheckOut();
            $firstSchkind = $db->getOne(knjj040Query::getSchkind($this));
            Query::dbCheckIn($db);
            $this->schkind = ($this->schkind) ? $this->schkind : $firstSchkind;
        }
    }

    function schreg_chk($schregno) {
        $row = knjj040Query::getSchregregd_data($this, CTRL_YEAR, CTRL_SEMESTER, $schregno);
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
            if ($this->field["SCHREGNO"] == "" || $this->field["CLUBCODEALL"] == "" || $this->field["SDATE"] == "") {
                $this->setWarning("MSG301");
                return false;
            }

            //重複チェック
            $row = knjj040Query::getClubHistory_Dat($this);
            if (is_array($row)) {
                $this->setWarning("MSG918", "(学籍番号/氏名),(部/クラブ),(入部日付)");
                return false;
            }

            //校種チェック（学籍番号と校種に矛盾がないか）
            if ($this->Properties["useClubMultiSchoolKind"] != '1' && $this->Properties["use_prg_schoolkind"] == "1") {
                $row = knjj040Query::getSchregKind($this);
                if (!is_array($row)) {
                    $this->setWarning("MSG303", "(学籍番号/校種)");
                    return false;
                }
            }
        }

        if ($cmd == "update") {
            //リストより選択しているか
            if ($this->gakusekino == "" || $this->clubcd == "" || $this->enterdate == "") {
                $this->setWarning("MSG308");
                return false;
            }
            //必須項目が変更されていないか
            if ($this->field["SCHREGNO"] != $this->gakusekino) {
                $this->setWarning("MSG308","(学籍番号/氏名)");
                return false;
            }
            if ($this->field["CLUBCODEALL"] != $this->clubcd) {
                $this->setWarning("MSG308","(部/クラブ)");
                return false;
            }
            if (str_replace("/","-",$this->field["SDATE"]) != str_replace("/","-",$this->enterdate)) {
                $this->setWarning("MSG308","(入部日付)");
                return false;
            }
            //存在チェック
            $row = knjj040Query::getClubHistory_Dat($this);
            if (!is_array($row)) {
                $this->setWarning("MSG303");
                return false;
            }

            //記録備考データ存在チェック
            $cnt = knjj040Query::chkDetailExist($this, $cmd);
            if($cnt > 0){
                $this->setWarning("MSG901","（退部日付：記録備考データ有）");
                return false;
            }
        }

        //文字数チェック
        if (strlen($this->field["REMARK"]) > 60) {
            $this->setWarning("MSG913", "(備考) 60バイトまで");
            return false;
        }
        //日付の大小チェック
        if ($this->field["EDATE"] && (str_replace("/","-",$this->field["SDATE"]) > str_replace("/","-",$this->field["EDATE"]))) {
            $this->setWarning("MSG901","(入部/退部日付の大小)");
            return false;
        }

        return true;
    }

    //編集完了用のモデルを返します
    function &getUpdateModel() {
        if ($this->validateInput("update")) {
            if ($this->update()) {
                $this->setMessage("MSG201");
                $this->gakusekino = $this->field["SCHREGNO"];
                $this->clubcd     = $this->field["CLUBCODEALL"];
                $this->enterdate  = $this->field["SDATE"];
                return;
            }
        }
    }

    function &getInsertModel() {
        if ($this->validateInput("add")) {
            if ($this->insert()) {
                $this->setMessage("MSG201");
                $this->gakusekino = $this->field["SCHREGNO"];
                $this->clubcd     = $this->field["CLUBCODEALL"];
                $this->enterdate  = $this->field["SDATE"];
                return;
            }
        }
    }

    function &getDeleteModel() {
        // 権限チェック
        if (common::SecurityCheck(STAFFCD, PROGRAMID) != DEF_UPDATABLE && common::SecurityCheck(STAFFCD, PROGRAMID) != DEF_UPDATE_RESTRICT ){
            $this->setWarning("MSG300");
            return false;
        }

        if ($this->gakusekino == "" || $this->clubcd == "" || $this->enterdate == "") {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->field["SCHREGNO"] != $this->gakusekino) {
            $this->setWarning("MSG308","(学籍番号/氏名)");
            return false;
        }
        if ($this->field["CLUBCODEALL"] != $this->clubcd) {
            $this->setWarning("MSG308","(部/クラブ)");
            return false;
        }
        if (str_replace("/","-",$this->field["SDATE"]) != str_replace("/","-",$this->enterdate)) {
            $this->setWarning("MSG308","(入部日付)");
            return false;
        }
        $row = knjj040Query::getClubHistory_Dat($this);
        if ($row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning("MSG307");
            return false;
        }

        //記録備考データ存在チェック
        $cnt = knjj040Query::chkDetailExist($this, 'delete');
        if ($cnt > 0) {
            $this->setWarning("MSG919","（記録備考データ有）");
            return false;
        }

        //削除
        if ($this->delete()) {
            $this->setMessage("MSG201");
            return;
        }
    }

    function &update() {
        $result = knjj040Query::getUpdateQuery($this, $this->field);
        return $result;
    }

    function &insert() {
        $result = knjj040Query::getInsertQuery($this, $this->field);
        return $result;
    }

    function &delete() {
        $result = knjj040Query::getDeleteQuery($this, $this->field);
        return $result;
    }

    //編集完了用のモデルを返します
    function &getSubInsertModel() {
        $cnt = knjj040Query::getSubInsertQuery($this);
        $sch_cnt = get_count(explode(',', $this->field["SUB_SCHREGNO"]));

        if ($cnt == $sch_cnt) {
            $this->setMessage("在籍しているため更新しませんでした。");
        } else if($cnt > 0) {
            $this->setMessage("MSG201","在籍者".$cnt."人は更新しませんでした。");
        } else {
            $this->setMessage("MSG201");
        }
        return;
    }

    //編集完了用のモデルを返します
    function &getSubUpdateModel() {
        $judge = knjj040Query::getSubUpdateQuery($this);

        if ($judge == "0") {
            $this->setMessage("MSG201");
        } else if ($judge == "1") {
            $this->setMessage("MSG913","　　　　　　　　　（退部日付）");
        } else {
            $this->setMessage("一部の生徒を更新しました。");
        }
        return;
    }
}
?>
