<?php

require_once('for_php7.php');

class knjd219Model extends Model {

    var $cmd;
    var $check = array();
    var $repGetCredit;
    var $repValue;

    function knjd219Model() {}

    function init() {
        //共通関数(コントロールマスタより)
        common::GetControlMaster_Fnc($this->control);
        $this->cmd = VARS::request("cmd");
        $this->check = VARS::post("CHECK");
        //合併元科目の「評定＝１（履修のみ）」を合併先科目（単位加算）では「修得」として扱うフラグの取得
        $rtnRow = knjd219Query::getNameMstD015();
        $this->repGetCredit = $rtnRow["NAMESPARE1"];
        $this->repValue = $rtnRow["NAMESPARE2"];
        //プロパティファイル
        $this->getPropertiesAll();
    }

    function validateInput($db)
    {
        if (get_count($this->check)) {
            //合併先科目の講座名簿の存在チェック
            $db = Query::dbCheckOut();
            foreach ($this->check as $val) {
                $arrExe = explode("-", $val);
                $combSubclass = $arrExe[0];//合併先科目コード
                $gvalCalc = $arrExe[1];//評定計算方法
                $combSubclassName = $arrExe[2];//合併先科目名
                //教育課程対応
                if ($this->Properties["useCurriculumcd"] == '1') {
                    $combClass = $arrExe[3];//合併先教科コード
                    $combSchoolkind = $arrExe[4];//合併先学校校種
                    $combCurriculumcd = $arrExe[5];//合併先教育課程コード
                }
                $query = knjd219Query::getExistsChair(CTRL_YEAR, $combSubclass, $combClass, $combSchoolkind, $combCurriculumcd, $this);
                $dataCnt = $db->getOne($query);
                if ($dataCnt == 0) {
                    $this->setWarning("MSG305","『" .$combSubclassName ."』の講座名簿が存在しません。");
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
        if ($this->validateInput($db)) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);
            //「評定を設定しない科目」の教科コードを配列にセット
            $classcdD008 = array();
            if ($this->Properties["useClassDetailDat"] == '1') {
                $results = $db->query(knjd219Query::getClassDetailDat());
            } else {
                $results = $db->query(knjd219Query::getNameMstD008());
            }
            while( $row = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
                $classcdD008[] = $row["CLASSCD"];
            }
            $results->free();

            $calcRow = $db->getRow(knjd219Query::getCalcDateTime(), DB_FETCHMODE_ASSOC);
            $calcDate = $calcRow["CALC_DATE"];//実行日付
            $calcTime = $calcRow["CALC_TIME"];//実行時間
            foreach ($this->check as $val) {
                $arrExe = explode("-", $val);
                $combSubclass = $arrExe[0];//合併先科目コード
                $gvalCalc = $arrExe[1];//評定計算方法
                //教育課程対応
                if ($this->Properties["useCurriculumcd"] == '1') {
                    $combClass = $arrExe[3];//合併先教科コード
                    $combSchoolkind = $arrExe[4];//合併先学校校種
                    $combCurriculumcd = $arrExe[5];//合併先教育課程コード
                }
                //科目合併評定自動計算履歴データ・追加
                $query = knjd219Query::getInsertRireki(CTRL_YEAR, $combSubclass, $gvalCalc, $calcDate, $calcTime, $combClass, $combSchoolkind, $combCurriculumcd, $this);
                $db->query($query);
                //①RECORD_DATに存在しない生徒を追加
                $query = knjd219Query::getInsertNonStdRec(CTRL_YEAR, $combSubclass, $combClass, $combSchoolkind, $combCurriculumcd, $this);
                $db->query($query);
                // 「評定を設定しない科目」の場合
                $classcd = substr($combSubclass, 0, 2);
                if (in_array($classcd, $classcdD008) || $classcd == "90") {
                    //②－Ａ）合併先科目のレコードが存在する生徒について、
                    //②－Ｂ）対応する合併元科目のレコードを検索し、
                    //③合併先科目の履修・修得単位を自動計算。
                    $query  = knjd219Query::getCreditUpdateList(CTRL_YEAR, $combSubclass, $combClass, $combSchoolkind, $combCurriculumcd, $this);
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $schno      = $row["SCHREGNO"];
                        $gradValue  = "";
                        $compCredit = $row["COMP_CREDIT"];
                        $getCredit  = $row["GET_CREDIT"];
                        //RECORD_DATの「評定」および「履修・修得単位」を更新。
                        $query = knjd219Query::getUpdateRecordDat(CTRL_YEAR, $combSubclass, $schno, $gradValue, $compCredit, $getCredit, $combClass, $combSchoolkind, $combCurriculumcd, $this);
                        $db->query($query);
                    }
                    $result->free();
                // 「評定を設定する科目」の場合
                } else {
                    //②－Ａ）合併先科目のレコードが存在する生徒について、
                    //②－Ｂ）対応する合併元科目のレコードを検索し、
                    //③合併先科目の評定および履修・修得単位を自動計算。
                    $query  = knjd219Query::getUpdateList($this->repGetCredit, $this->repValue, CTRL_YEAR, $combSubclass, $gvalCalc, $combClass, $combSchoolkind, $combCurriculumcd, $this);
                    $result = $db->query($query);
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $schno      = $row["SCHREGNO"];
                        //変数に値をセット
                        $gradValue  = $row["VALUE"];
                        $compCredit = $row["COMP_CREDIT"];
                        $getCredit  = $row["GET_CREDIT"];
                        //評定が２以上
                        if (1 < $gradValue) {
                        //評定が１・・・修得単位にゼロをセット
                        } else if (1 == $gradValue) {
                            $getCredit  = 0;
                        //評定がNULL（履修がゼロでない かつ 修得がゼロ）・・・評定に１をセット
                        } else if (0 < $compCredit && $getCredit == 0) {
                            $gradValue  = 1;
                        }
                        //RECORD_DATの「評定」および「履修・修得単位」を更新。
                        $query = knjd219Query::getUpdateRecordDat(CTRL_YEAR, $combSubclass, $schno, $gradValue, $compCredit, $getCredit, $combClass, $combSchoolkind, $combCurriculumcd, $this);
                        $db->query($query);
                    }
                    $result->free();
                }
            }

            $db->commit();
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }
}
?>
