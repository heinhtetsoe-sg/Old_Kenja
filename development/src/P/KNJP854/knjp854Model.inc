<?php

require_once('for_php7.php');

class knjp854Model extends Model
{
    public $field        = array();
    public $cmd;
    public $schregno;
    public $name;          //生徒氏名

    public function knjp854Model()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->year);
            unset($this->schregno);
            unset($this->name);
            unset($this->schoolKind);
            unset($this->field);
            unset($this->field2);
            unset($this->field3);
        }

        //親画面から呼び出されたときの生徒検索画面の条件
        if (VARS::get("SEND_SCHREGNO") != "") {
            $this->sendSchregno  = VARS::get("SEND_SCHREGNO");
        }

        $this->year     = CTRL_YEAR;
        $this->semester = CTRL_SEMESTER;
        $this->schoolCd = sprintf("%012d", SCHOOLCD);
        $this->cmd = VARS::request("cmd");

        //行数
        $this->rowCnt       = VARS::post("ROWCNT");
        $this->noDispRowCnt = VARS::post("NO_DISP_ROWCNT");

        //編集画面
        $this->field = array("PLAN_YEAR_MONTH"        =>  VARS::post("PLAN_YEAR_MONTH"),                               //納期限月
                             "PLAN_PAID_MONEY_DATE"   =>  str_replace("/", "-", VARS::post("PLAN_PAID_MONEY_DATE")),    //入金日
                             "PLAN_PAID_MONEY_DIV"    =>  VARS::post("PLAN_PAID_MONEY_DIV"),                            //入金方法
                             "SLIP_NO"                =>  VARS::post("SLIP_NO"),                                        //伝票番号
                             "AUTO_KESIKOMI"          =>  VARS::post("AUTO_KESIKOMI"),                                  //自動消込
                             "AUTO_KESIKOMI_MONEY"    =>  VARS::post("AUTO_KESIKOMI_MONEY"));                           //入金額合計

        //明細行
        $this->field2 = array();

        //徴収予定額が0円の行
        $this->field3 = array();

        //合計行金額
        $this->totalRow = array();
        $this->totalRow["DISP_PLAN_MONEY_TOTAL"] = VARS::post("DISP_PLAN_MONEY_TOTAL");
        $this->totalRow["PAID_MONEY_TOTAL"]      = VARS::post("PAID_MONEY_TOTAL");
        $this->totalRow["BALANCE_TOTAL"]         = VARS::post("BALANCE_TOTAL");

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno    = VARS::get("SCHREGNO");
        }

        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //DB接続
        $db = Query::dbCheckOut();

        //校種取得
        $this->schoolKind = $db->getOne(knjp854Query::getSchregSchKindQuery($this));

        //グループコード番号帯取得
        $p012Row = $db->getRow(knjp854Query::getNameMstP012($this), DB_FETCHMODE_ASSOC);
        if ($p012Row) {
            $this->grpCdFromNo = $p012Row["NAME2"];
            $this->grpCdToNo   = $p012Row["NAME3"];
        }

        //割振りグループコード設定
        $query = knjp854Query::getMaxCollectGrpCdQuery($this);
        $maxGrpCd = $db->getOne($query);
        $tmp = ($maxGrpCd != "") ?  $maxGrpCd + 1 : $this->grpCdFromNo;
        $this->nextGrpCd = sprintf("%04d", $tmp);

        Query::dbCheckIn($db);
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        if ($this->schregno == "") {
            $this->setWarning("MSG304", "(左より生徒を選択してから行ってください)");
            return false;
        }

        return true;
    }

    public function &getUpdateModel()
    {
        for ($i = 1; $i <= $this->rowCnt; $i++) {
            $this->field2[$i]["ROW_KEY"]         = VARS::post("ROW_KEY_".$i);
            $this->field2[$i]["DISP_PLAN_MONEY"] = VARS::post("DISP_PLAN_MONEY_".$i);
            $this->field2[$i]["PAID_MONEY"]      = VARS::post("PAID_MONEY_".$i);
            $this->field2[$i]["BALANCE"]         = $this->field2[$i]["DISP_PLAN_MONEY"] - $this->field2[$i]["PAID_MONEY"];
            $this->field2[$i]["REDUCE_MONEY"]    = VARS::post("REDUCE_MONEY_".$i);
        }
        for ($i = 1; $i <= $this->noDispRowCnt; $i++) {
            $this->field3[$i]["NO_DISP_ROW_KEY"]    = VARS::post("NO_DISP_ROW_KEY_".$i);
            $this->field3[$i]["NO_DISP_PLAN_MONEY"] = VARS::post("NO_DISP_PLAN_MONEY_".$i);
        }

        if ($this->validateInput("update")) {
            $this->update();
            $this->field["PLAN_YEAR_MONTH"] = "";
            $this->field["SLIP_NO"] = "";
        }
    }

    //更新
    public function &update()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $updFlg = knjp854Query::getUpdateQuery($db, $this);
        if ($updFlg) {
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("MSG303");
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
