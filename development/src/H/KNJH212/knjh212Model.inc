<?php

require_once('for_php7.php');

class knjh212Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ

    var $SelectDomi; //左側で選んだ寮
    var $domiflg;    //左側で寮が変更されたかどうか

    var $gakusekino; //キー１
    var $domicd;     //キー２
    var $enterdate;  //キー３

    var $gakusekino2;

    var $namecd1 = "J001";  //固定値（名称区分）

    function knjh212Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        if (VARS::get("PROGRAMID")) {
            unset($this->SelectDomi);
            unset($this->GradeClass);
            unset($this->gakusekino);
            unset($this->domicd);
            unset($this->enterdate);
        }

        $this->field = array(
//                    /*-------          domitory_history_dat テーブル用         --------------*/
                             "SCHREGNO"         =>  VARS::request("SCHREGNO"),      //在校生検索ボタンのプログラムからの学籍番号
                             "DOMI_CODE1"       =>  VARS::post("DOMI_CODE1"),       //寮コード
                             "DOMI_CODEALL"     =>  VARS::post("DOMI_CODEALL"),     //寮コード（4桁）
                             "DOMI_ENTDAY"      =>  VARS::post("DOMI_ENTDAY"),      //入寮日
                             "DOMI_OUTDAY"      =>  VARS::post("DOMI_OUTDAY"),      //退寮日
                             "UPDATED"          =>  VARS::post("UPDATED"),          //更新日付

                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //クラス
                             "CLUB"             =>  VARS::post("CLUB"),             //部活
                             "SUB_SDATE"        =>  VARS::post("SUB_SDATE"),        //入寮日
                             "SUB_EDATE"        =>  VARS::post("SUB_EDATE"),        //退寮日
                             "SUB_SCHREGNO"     =>  VARS::post("selectdata"),       //学籍番号

                            );

        $this->cmd = VARS::request("cmd");

        //寮選択リスト
        if (VARS::request("DOMI_YEAR_LIST")) {
            $this->SelectDomi = VARS::request("DOMI_YEAR_LIST");
        } else if (VARS::request("RELOADDOMI")) {
            $this->SelectDomi = VARS::request("RELOADDOMI");
        }
        //更新後この行にスクロールバーを移動させる(左フレームのこと)ために使う変数(gakusekino2)
        if (VARS::request("GAKUSEKINO")) {
            $this->gakusekino2 = VARS::request("GAKUSEKINO");
        }

        //左フレームで寮が変わったかどうかのフラグ
        if (VARS::request("DOMI_FLG")) {
            $this->domiflg = VARS::request("DOMI_FLG");
        } else {
            $this->domiflg = "";
        }

        //list→edit
        if (VARS::get("GAKUSEKINO")) {
            $this->gakusekino = VARS::get("GAKUSEKINO");
        }
        if (VARS::get("DOMI_CD")) {
            $this->domicd = VARS::get("DOMI_CD");
        }
        if (VARS::get("DOMI_ENTDAY")) {
            $this->enterdate = VARS::get("DOMI_ENTDAY");
        }
        if(VARS::request("SCHKIND") != ""){
            $this->schkind = VARS::request("SCHKIND");
        }

        //校種初期値（プログラムを開き、左画面は何も触らないで [追加] する際、
        //            $this->schkind がセットされない時があるため（原因不明））
        $db = Query::dbCheckOut();
        $firstSchkind = $db->getOne(knjh212Query::getSchkind($this));
        Query::dbCheckIn($db);
        $this->schkind = ($this->schkind) ? $this->schkind : $firstSchkind;

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    function schreg_chk($schregno) {
        $row = knjh212Query::getSchregregd_data($this, CTRL_YEAR, CTRL_SEMESTER, $schregno);
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
            if ($this->field["SCHREGNO"] == "" || $this->field["DOMI_CODEALL"] == "" || $this->field["DOMI_ENTDAY"] == "") {
                $this->setWarning("MSG301");
                return false;
            }

            //キーチェック
            $histcheck = knjh212Query::getDomitoryHistory_key_chk($this->field["SCHREGNO"], $this->field["DOMI_ENTDAY"]); /* NO001 */
            if (is_array($histcheck)){          /* NO001 */
                $this->setWarning("MSG918", "(学籍番号/氏名),(寮コード),(入寮日)");
                return false;
            }
            //重複日チェック
            $row = knjh212Query::getDomitoryHistory_Dat_chk($this->field["SCHREGNO"], $this->field["DOMI_ENTDAY"], $this->field["DOMI_OUTDAY"]);
            if (is_array($row)) {
                $this->setWarning("MSG918", "(学籍番号/氏名),(寮コード),(入寮日)");
                return false;
            }
        }

        if ($cmd == "update") {
            //リストより選択しているか
            if ($this->gakusekino == "" || $this->domicd == "" || $this->enterdate == "") {
                $this->setWarning("MSG308");
                return false;
            }
            //必須項目が変更されていないか
            if ($this->field["SCHREGNO"] != $this->gakusekino) {
                $this->setWarning("MSG308","(学籍番号/氏名)");
                return false;
            }
            if ($this->field["DOMI_CODEALL"] != $this->domicd) {
                $this->setWarning("MSG308","(寮コード)");
                return false;
            }
            if (str_replace("/","-",$this->field["DOMI_ENTDAY"]) != str_replace("/","-",$this->enterdate)) {
                $this->setWarning("MSG308","(入寮日)");
                return false;
            }
            ////キーチェック
            //更新処理のため、キーチェックは不要。
            //$histcheck = knjh212Query::getDomitoryHistory_key_chk($this->field["SCHREGNO"], $this->field["DOMI_ENTDAY"]); /* NO001 */
            //if (is_array($histcheck)){          /* NO001 */
            //    $this->setWarning("MSG918", "(学籍番号/氏名),(寮コード),(入寮日)");
            //    return false;
            //}
            //重複日チェック
            $row = knjh212Query::getDomitoryHistory_Dat_chk($this->field["SCHREGNO"], $this->field["DOMI_ENTDAY"], $this->field["DOMI_OUTDAY"]);
            if (is_array($row)) {
                $this->setWarning("MSG302");
                return false;
            }
        }

        //文字数チェック
        if (strlen($this->field["REMARK"]) > 60) {
            $this->setWarning(MSG913, "(備考) 60バイトまで");
            return false;
        }
        //日付の大小チェック
        if ($this->field["DOMI_OUTDAY"] && (str_replace("/","-",$this->field["DOMI_ENTDAY"]) > str_replace("/","-",$this->field["DOMI_OUTDAY"]))) {
            $this->setWarning("MSG901","(入寮/退寮日の大小)");
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
                $this->domicd     = $this->field["DOMI_CODEALL"];
                $this->enterdate  = $this->field["DOMI_ENTDAY"];
                return;
            }
        }
    }

    function &getInsertModel() {
        if ($this->validateInput("add")) {
            if ($this->insert()) {
                $this->setMessage("MSG201");
                $this->gakusekino = $this->field["SCHREGNO"];
                $this->domicd     = $this->field["DOMI_CODEALL"];
                $this->enterdate  = $this->field["DOMI_ENTDAY"];
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

        if ($this->gakusekino == "" || $this->domicd == "" || $this->enterdate == "") {
            $this->setWarning("MSG308");
            return false;
        }
        if ($this->field["SCHREGNO"] != $this->gakusekino) {
            $this->setWarning("MSG308","(学籍番号/氏名)");
            return false;
        }
        if ($this->field["DOMI_CODEALL"] != $this->domicd) {
            $this->setWarning("MSG308","(寮コード)");
            return false;
        }
        if (str_replace("/","-",$this->field["DOMI_ENTDAY"]) != str_replace("/","-",$this->enterdate)) {
            $this->setWarning("MSG308","(入寮日)");
            return false;
        }
        $row = knjh212Query::getDomitoryHistory_Dat($this);
        if ($row["UPDATED"] != $this->field["UPDATED"]) {
            $this->setWarning("MSG307");
            return false;
        }

        //削除
        if ($this->delete()) {
            $this->setMessage("MSG201");
            return;
        }
    }

    function &update() {
        $result = knjh212Query::getUpdateQuery($this, $this->field);
        return $result;
    }

    function &insert() {
        $result = knjh212Query::getInsertQuery($this, $this->field);
        return $result;
    }

    function &delete() {
        $result = knjh212Query::getDeleteQuery($this, $this->field);
        return $result;
    }

    //編集完了用のモデルを返します
    function &getSubInsertModel() {
        $cnt = knjh212Query::getSubInsertQuery($this);
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
        $judge = knjh212Query::getSubUpdateQuery($this);

        if ($judge == "0") {
            $this->setMessage("MSG201");
        } else if ($judge == "1") {
            $this->setMessage("MSG913","　　　　　　　　　（退寮日）");
        } else {
            $this->setMessage("一部の生徒を更新しました。");
        }
        return;
    }
}
?>
