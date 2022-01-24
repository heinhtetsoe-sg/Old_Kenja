<?php

require_once('for_php7.php');


class knjd133aModel extends Model {
    var $field      = array();
    var $cmd;
    var $selectdata;
    var $subclassArray = array();

    function knjd133aModel()
    {
    }
    function init()
    {               
        $this->field = array("GRADE"            =>  VARS::post("GRADE"),            //学年
                             "ATTEND_DATE"      =>  VARS::post("ATTEND_DATE"),      //出欠対象日付
                             "SHORI"            =>  VARS::post("SHORI")             //欠課時数と全員のラジオ
                     );

        $this->cmd         = VARS::request("cmd");

        //処理対象科目(左のリスト）
        $this->selectdata = VARS::post("selectdata");
        $this->subclassArray = (strlen($this->selectdata) ? explode(",", $this->selectdata) : array());
        //プロパティファイルの読込み
        $this->getPropertiesAll();
    }

    function validateInput()
    {
        //科目の講座名簿の存在チェック
        $db = Query::dbCheckOut();
        foreach ($this->subclassArray as $key => $subclasscd) {
            $query = knjd133aQuery::getExistsChair(CTRL_YEAR, CTRL_SEMESTER, $subclasscd, $this->field["GRADE"], $this);
            $dataCnt = $db->getOne($query);
            if ($dataCnt == 0) {
                $subclassName = $db->getOne(knjd133aQuery::getSubclassName($subclasscd, $this));//科目名
                Query::dbCheckIn($db);
                $this->setWarning("MSG305","『" .$subclassName ."』の講座名簿が存在しません。");
                return false;
            }
        }
        Query::dbCheckIn($db);
        return true;
    }

    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            //履修上限値を超えた生徒・科目
            $jougenOverArray = ($this->field["SHORI"] == "1" ? $this->getJougenOverList() : array());

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $calcRow = $db->getRow(knjd133aQuery::getCalcDateTime(), DB_FETCHMODE_ASSOC);
            $calcDate = $calcRow["CALC_DATE"];//実行日付
            $calcTime = $calcRow["CALC_TIME"];//実行時間
            foreach ($this->subclassArray as $key => $subclasscd) {
                //実行履歴データ・追加
                $query = knjd133aQuery::getInsertRireki($this, $calcDate, $calcTime, $subclasscd);
                $db->query($query);
                //①RECORD_DATに存在しない生徒を追加
                $query = knjd133aQuery::getInsertNonStdRec(CTRL_YEAR, CTRL_SEMESTER, $subclasscd, $this->field["GRADE"], $this);
                $db->query($query);
                //②科目毎・生徒毎に科目の単位を自動計算し、RECORD_DATに登録する。
                $query  = knjd133aQuery::getCreditUpdateList($this, CTRL_YEAR, CTRL_SEMESTER, $subclasscd);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $schno      = $row["SCHREGNO"];
                    $gradValue  = "";
                    $compCredit = $row["COMP_CREDIT"];
                    $getCredit  = $row["GET_CREDIT"];
                    //欠課時数で判断する場合、履修上限値を超えていたら、履修・修得の単位数はセットしない
                    $serchVal = $subclasscd ."-" .$schno;
                    if (in_array($serchVal, $jougenOverArray) && $this->field["SHORI"] == "1") {
                        $compCredit = "";
                        $getCredit  = "";
//echo "schno=" .$schno .", subclasscd=" .$subclasscd ."<BR>";
                    }
                    //RECORD_DATの「評定」および「履修・修得単位」を更新。
                    $query = knjd133aQuery::getUpdateRecordDat(CTRL_YEAR, $subclasscd, $schno, $gradValue, $compCredit, $getCredit, $this);
                    $db->query($query);
                }
                $result->free();
            }

            $db->commit();
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
    }

    //履修上限値を超えた生徒・科目
    function getJougenOverList() {
        //DB接続
        $db = Query::dbCheckOut();

        //出欠集計開始日付などを取得(端数処理に使用)
        $attend_sdate = "";
        $attend_seme = "";
        $attend_month = array();
        $query = knjd133aQuery::getAttendDate();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $tmp_attend_sdate = $row["MAX_YEAR"] ."-" .$row["MONTH"] ."-" .$row["MAX_APP"];
            if (str_replace("/","-",$this->field["ATTEND_DATE"]) < $tmp_attend_sdate) break;
            $attend_month[] = $row["MONTH"];
            $attend_sdate = $tmp_attend_sdate;
            $attend_seme = $row["SEMESTER"];
        }
        $result->free();
        if ($attend_sdate == "") {
            $query2 = "SELECT SDATE FROM SEMESTER_MST WHERE YEAR='".CTRL_YEAR."' AND SEMESTER='1'";
            $attend_sdate = $db->getOne($query2);   //学期開始日
        } else {
            $query2 = "VALUES Add_days(date('".$attend_sdate."'), 1)";
            $attend_sdate = $db->getOne($query2);   //次の日
        }
        //SCHOOL_MSTの情報を取得。
        $knjSchoolMst = $db->getRow(knjd133aQuery::getSchoolMst(CTRL_YEAR), DB_FETCHMODE_ASSOC);
        //履修上限値を超えた生徒・科目
        $query = knjd133aQuery::getRishuJougenOver($this, $attend_seme, $attend_month, $attend_sdate, $knjSchoolMst);
//echo $query;
        $result = $db->query($query);
        $dataArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataArray[] = $row["SUBCLASSCD"] ."-" .$row["SCHREGNO"];
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //配列(履修上限値を超えた生徒・科目)
        return $dataArray;
    }
}
?>
