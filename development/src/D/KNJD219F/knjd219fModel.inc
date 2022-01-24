<?php

require_once('for_php7.php');

class knjd219fModel extends Model {

    var $field = array();
    var $cmd;
    var $check = array();

    function knjd219fModel() {}

    function init() {
        //共通関数(コントロールマスタより)
        common::GetControlMaster_Fnc($this->control);
        $this->cmd = VARS::request("cmd");
        $this->check = VARS::post("CHECK");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->field = array("SCHKIND"      => VARS::post("SCHKIND"),       //校種
                             "TEST_CD"      => VARS::post("TEST_CD")        //テスト種別
                            );
        $sem = substr($this->field["TEST_CD"], 0, 1);
        $this->semChair = ($sem == "9") ? CTRL_SEMESTER : $sem;
    }

    function validateInput() {
        if (get_count($this->check)) {
            //合併先科目の講座名簿の存在チェック
            $db = Query::dbCheckOut();
            foreach ($this->check as $val) {
                $arrExe = explode("_", $val);
                $dataCnt = $db->getOne(knjd219fQuery::getExistsChair($arrExe[0], $this));
                if ($dataCnt == 0) {
                    $this->setWarning("MSG305","『".$arrExe[2]."』の講座名簿が存在しません。");
                    return false;
                }
            }
            Query::dbCheckIn($db);
        } else {
            //選択チェック
            return false;
        }
        return true;
    }

    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //実行日付・時間を取得
            $calcRow = $db->getRow(knjd219fQuery::getCalcDateTime(), DB_FETCHMODE_ASSOC);

            foreach ($this->check as $val) {
                $arrExe = explode("_", $val);
                $combSubclass = $arrExe[0]; //合併先科目コード
                $gvalCalc = $arrExe[1];     //計算方法
                //科目合併成績自動計算履歴データ・追加
                $query = knjd219fQuery::getInsertRireki($combSubclass, $gvalCalc, $calcRow, $this);
                $db->query($query);
                //成績データに合併先科目のレコードが存在しない生徒を追加
                $query = knjd219fQuery::getInsertNonStdRec($combSubclass, $this);
                $db->query($query);
                //合併先科目のレコードが存在する生徒について、
                //対応する合併元科目のレコードを検索し、
                //合併先科目の成績を自動計算。
                $seme = substr($this->field["TEST_CD"], 0, 1);
                $kind = substr($this->field["TEST_CD"], 1, 2);
                $item = substr($this->field["TEST_CD"], 3, 2);
                $div  = substr($this->field["TEST_CD"], 5, 2);
                $subclasscdArray = explode("-", $combSubclass);
                $query  = knjd219fQuery::getUpdateList($combSubclass, $gvalCalc, $this);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //成績データを更新。
                    $query = knjd219fQuery::getUpdateRecordDat($seme, $kind, $item, $div, $subclasscdArray, $gvalCalc, $row, $this);
                    if ($row["SCORE_MAX"] == '' && $this->Properties["kessiJogai_create_sakiScore"] == "1") {
                        $query = knjd219fQuery::getDeleteRecordDat($seme, $kind, $item, $div, $subclasscdArray, $row);
                    }
                    $db->query($query);
                }
                $result->free();
            }

            $db->commit();
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }
}
?>
