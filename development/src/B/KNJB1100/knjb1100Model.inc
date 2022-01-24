<?php

require_once('for_php7.php');

class knjb1100Model extends Model {

    var $cmd;
    var $semester;
    var $dataList = array();
    var $check = array();
    var $field = array();

    function knjb1100Model() {}

    function init() {
        //共通関数(コントロールマスタより)
        common::GetControlMaster_Fnc($this->control);
        $this->cmd = VARS::request("cmd");
        $this->semester = VARS::post("SEMESTER");
        $this->check = VARS::post("CHECK");
        for ($i = 0; $i < get_count($this->dataList); $i++) {
            $this->field["CHAIRCD" .$i] = VARS::post("CHAIRCD" .$i);
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    function validateInput() {
        if (get_count($this->check)) {
            $db = Query::dbCheckOut();
            foreach ($this->check as $val) {
                $arrExe = explode(":", $val);
                $combSubclass = $arrExe[0];//合併先科目コード
                $combChair = $arrExe[1];//合併先講座コード
                $combGroup = $arrExe[2];//合併先群コード
                $combSubclassName = $arrExe[3];//合併先科目名
                //合併元科目の講座名簿の存在チェック
                $query = knjb1100Query::getExistsMoto($this, CTRL_YEAR, $this->semester, $combSubclass);
                $dataCnt = $db->getOne($query);
                if ($dataCnt == 0) {
                    $this->setWarning("MSG303","『" .$combSubclassName ."』の合併元科目の講座名簿が存在しません。");
                    return false;
                }
                //群チェック
                if ($combGroup != "0000") {
                    $this->setWarning("MSG916","群に属する講座は処理できません。");
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

            //名簿開始日・終了日（学期開始日・終了日）
            $appdateRow = array();
            $appdateRow = $db->getRow(knjb1100Query::getAppdate(CTRL_YEAR, $this->semester), DB_FETCHMODE_ASSOC);
            $appSdate = $appdateRow["SDATE"];
            $appEdate = $appdateRow["EDATE"];
            foreach ($this->check as $val) {
                $arrExe = explode(":", $val);
                $combSubclass = $arrExe[0];//合併先科目コード
                $combChair = $arrExe[1];//合併先講座コード
                $combGroup = $arrExe[2];//合併先群コード
                //①CHAIR_STD_DATを生成
                //削除
                $query = knjb1100Query::getDeleteChairStdDat(CTRL_YEAR, $this->semester, $combChair);
                $db->query($query);
                //合併元科目の名簿（生徒）を取得し生成
                $query  = knjb1100Query::getUpdateSchno($this, CTRL_YEAR, $this->semester, $combSubclass);
                $resStd = $db->query($query);
                while ($rowStd = $resStd->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $schno = $rowStd["SCHREGNO"];
                    $query = knjb1100Query::getInsertChairStdDat(CTRL_YEAR, $this->semester, $combChair, $schno, $appSdate, $appEdate);
                    $db->query($query);
                }
                $resStd->free();
                //②CHAIR_CLS_DATを生成
                //削除
                $query = knjb1100Query::getDeleteChairClsDat(CTRL_YEAR, $this->semester, $combChair, $combGroup);
                $db->query($query);
                //生成された名簿より受講クラスを取得
                $query  = knjb1100Query::getUpdateClass(CTRL_YEAR, $this->semester, $combChair);
                $resCls = $db->query($query);
                while ($rowCls = $resCls->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $trgtgrade = $rowCls["GRADE"];
                    $trgtclass = $rowCls["HR_CLASS"];
                    $query = knjb1100Query::getInsertChairClsDat(CTRL_YEAR, $this->semester, $combChair, "0000", $trgtgrade, $trgtclass);
                    $db->query($query);
                }
                $resCls->free();
            }

            $db->commit();
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }
}
?>
