<?php

require_once('for_php7.php');

class knjp070kModel extends Model {
    var $field      = array();
    var $cmd;
    var $schregno;
    var $sex;		//NO001

    var $control_data;

    function knjp070kModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
        $this->div = "M";
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");

        if(VARS::get("EXPENSE_M_CD") != ""){
            $this->expense_m_cd = VARS::get("EXPENSE_M_CD");
        }
        
        if(VARS::get("EXPENSE_S_CD") != "") {
            $this->expense_s_cd = VARS::get("EXPENSE_S_CD");
        }

        $this->name     = VARS::get("name");
        $this->div      = VARS::request("radiodiv");

        $this->field = array("EXPENSE_M_CD"     => VARS::post("EXPENSE_M_CD"),
                             "EXPENSE_S_CD"     => VARS::post("EXPENSE_S_CD"),
                             "MONEY_DUE"        => VARS::post("MONEY_DUE"), 
                             "UN_AUTO_PAYFLG"   => VARS::post("UN_AUTO_PAYFLG"), 
                             "REDUCTION_REASON" => VARS::post("REDUCTION_REASON"));

        if (VARS::post("HRCLASS")!="") {
            $this->hrclass = VARS::post("HRCLASS");
        }

        if(VARS::get("GRADE") && VARS::get("HR_CLASS")) {
            $this->hrclass = VARS::get("GRADE") ."-".VARS::get("HR_CLASS");
        }
        $this->selectdata = VARS::post("selectdata");

        //変数初期化
        if(VARS::get("SCHREGNO") != "" || VARS::get("init") || VARS::get("PROGRAMID")) {
            $this->schregno = VARS::get("SCHREGNO");
            $this->div = "M";
            unset($this->field);
            $this->expense_m_cd = "";
            $this->expense_s_cd = "";
            unset($this->sex);	//NO001
        }
        if (VARS::get("radiodiv")) {
            $this->select_opt = array();
        }
		//NO001
        if (VARS::post("SEX") != "") {
            $this->sex = VARS::post("SEX");
        }
    }

    function validateInput()
    {
        if ($this->div == "M" && $this->field["EXPENSE_M_CD"]=="") {
            $this->setWarning("MSG301");
            return false;
        }
        if ($this->div == "S" && $this->field["EXPENSE_S_CD"]=="") {
            $this->setWarning("MSG301");
            return false;
        }
        if ($this->cmd == "add" && $this->schregno == "") {
            $this->warning = "左の生徒一覧から生徒を選択してください";
            return false;
        }

        if  (($this->cmd == "update" || $this->cmd == "delete") && $this->schregno == "") {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "delete") && 
            $this->div == "M" && $this->expense_m_cd != $this->field["EXPENSE_M_CD"]) {
            $this->setWarning("MSG308");
            return false;
        }
        if (($this->cmd == "update" || $this->cmd == "delete") &&
            $this->div == "S" && $this->expense_s_cd != $this->field["EXPENSE_S_CD"]) {
            $this->setWarning("MSG308");
            return false;
        }
        return true;
    }

    //更新
    function &getUpdateModel()
    {
        //入力チェック
        if (!$this->validateInput())
            return;

        $db = Query::dbCheckOut();
        
        //入金済データチェック
        if ($this->div == "M") {
            $query = knjp070kQuery::CheckPaidData($this->div, $this->schregno, $this->field["EXPENSE_".$this->div."_CD"]);
        } else {
            $query = knjp070kQuery::CheckPaidSdata($this->schregno, $this->field["EXPENSE_".$this->div."_CD"]);
        }
        if (is_array($db->getRow($query))) {
            $this->setWarning("MSG203", "入金済データが存在します。");
            Query::dbCheckIn($db);
            return;
        }
        if ($this->div == "M") {
            $m_cd = $this->field["EXPENSE_".$this->div."_CD"];
        } else {
            $m_cd = $db->getOne(knjp070kQuery::getCheckMcd($this->field["EXPENSE_".$this->div."_CD"]));
        }
        //分納データ存在チェック
        $query = knjp070kQuery::CheckInstData($this->schregno, $m_cd);
        if (is_array($db->getRow($query))) {
            $this->setWarning("MSG203", "分納データが存在します。");
            Query::dbCheckIn($db);
            return;
        }
        //小分類編集時
        if ($this->div == "S") {

            //親費目(中分類)の入金済みデータチェック
            $query = knjp070kQuery::CheckPaidData("M", $this->schregno, $m_cd);
            if (is_array($db->getRow($query))) {
                $this->setWarning("MSG203", "入金済データが存在します。");
                Query::dbCheckIn($db);
                return;
            }
        }

        $query = knjp070kQuery::getUpdateQuery($this->div, $this->field, $this->schregno);
        $db->query($query);
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);
    }

    //削除
    function &getDeleteModel()
    {
        if (!$this->validateInput())
            return;

        $db = Query::dbCheckOut();

        //入金済データチェック
        if ($this->div == "M") {
            $query = knjp070kQuery::CheckPaidData($this->div, $this->schregno, $this->field["EXPENSE_".$this->div."_CD"]);
        } else {
            $query = knjp070kQuery::CheckPaidSdata($this->schregno, $this->field["EXPENSE_".$this->div."_CD"]);
        }
        if (is_array($db->getRow($query))) {
            $this->setWarning("MSG203", "入金済データが存在します。");
            Query::dbCheckIn($db);
            return;
        }
        if ($this->div == "M") {
            $m_cd = $this->field["EXPENSE_".$this->div."_CD"];
        } else {
            $m_cd = $db->getOne(knjp070kQuery::getCheckMcd($this->field["EXPENSE_".$this->div."_CD"]));
        }
        //分納データ存在チェック
        $query = knjp070kQuery::CheckInstData($this->schregno, $m_cd);
        if (is_array($db->getRow($query))) {
            $this->setWarning("MSG203", "分納データが存在します。");
            Query::dbCheckIn($db);
            return;
        }
        //小分類編集時
        if ($this->div == "S") {

            //親費目(中分類)の入金済みデータチェック
            $query = knjp070kQuery::CheckPaidData("M", $this->schregno, $m_cd);
            if (is_array($db->getRow($query))) {
                $this->setWarning("MSG203", "入金済データが存在します。");
                Query::dbCheckIn($db);
                return;
            }
        }

        $query = knjp070kQuery::getDeleteQuery($this->div, $this->field, $this->schregno);
        $db->query($query);

        //下位の小分類を削除
        if ($this->div == "M") {
            $query = knjp070kQuery::getDeleteQuery2($this->field, $this->schregno);
            $db->query($query);
        }

        $this->setMessage("MSG201");
        Query::dbCheckIn($db);
    }
    
    //追加
    function &getAddModel()
    {
        if (!$this->validateInput())
            return;

        $db = Query::dbCheckOut();
        //重複チェック
        $query = knjp070kQuery::CheckExistData($this->div, $this->schregno, $this->field["EXPENSE_".$this->div."_CD"]);
        if (is_array($db->getRow($query))) {
            $this->setWarning("MSG302");
            Query::dbCheckIn($db);
            return;
        }
        //小分類編集時
        if ($this->div == "S") {
            $m_cd = $db->getOne(knjp070kQuery::getCheckMcd($this->field["EXPENSE_".$this->div."_CD"]));
            $sex_cd = $db->getOne(knjp070kQuery::getCheckMcdSex($this->field["EXPENSE_".$this->div."_CD"]));
            $base_sex = $db->getOne(knjp070kQuery::getCheckBaseSex($this->schregno));

            if ($sex_cd != "" && $sex_cd != $base_sex) {
                $this->setWarning("MSG203", "この項目の性別対象ではありません。");
                Query::dbCheckIn($db);
                return;
            }
            //親費目(中分類)の分納データ存在チェック
            $query = knjp070kQuery::CheckInstData($this->schregno, $m_cd);
            if (is_array($db->getRow($query))) {
                $this->setWarning("MSG203", "分納データが存在します。");
                Query::dbCheckIn($db);
                return;
            }
            //親費目(中分類)の入金済みデータチェック
            $query = knjp070kQuery::CheckPaidData("M", $this->schregno, $m_cd);
            if (is_array($db->getRow($query))) {
                $this->setWarning("MSG203", "入金済データが存在します。");
                Query::dbCheckIn($db);
                return;
            }
        } else {
            $l_cd = $db->getOne(knjp070kQuery::getCheckLcd($this->field["EXPENSE_".$this->div."_CD"]));
            $query = knjp070kQuery::Check03Mdata($this->div, $this->schregno);
            if ($l_cd == "03" && is_array($db->getRow($query))) {
                $this->setWarning("MSG203", "生活行事費の登録は１つまでです。");
                Query::dbCheckIn($db);
                return;
            }
        }
        $query = knjp070kQuery::getAddQuery($this->div, $this->field, $this->schregno);
        $db->query($query);
        $this->setMessage("MSG201");
        $this->expense_m_cd = $this->field["EXPENSE_M_CD"];
        $this->expense_s_cd = $this->field["EXPENSE_S_CD"];
        Query::dbCheckIn($db);
    }
    
    //一括更新
    function &getAllUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = explode(",", $this->selectdata);

        if ($this->div == "M") {
            $m_cd = $this->field["EXPENSE_".$this->div."_CD"];
        } else {
            $m_cd = $db->getOne(knjp070kQuery::getCheckMcd($this->field["EXPENSE_".$this->div."_CD"]));
        }

        foreach ($data as $schregno)
        {
            //入金済データチェック
            if ($this->div == "M") {
                $query = knjp070kQuery::CheckPaidData($this->div, $schregno, $this->field["EXPENSE_".$this->div."_CD"]);
            } else {
                $query = knjp070kQuery::CheckPaidSdata($schregno, $this->field["EXPENSE_".$this->div."_CD"]);
            }
            if (is_array($db->getRow($query))) {
                $this->setWarning("MSG203", "入金済データが存在します。\\n\\n学籍番号：".$schregno);
                $db->rollback();
                Query::dbCheckIn($db);
                return;
            }
            //分納データ存在チェック
            $query = knjp070kQuery::CheckInstData($schregno, $m_cd);
            if (is_array($db->getRow($query))) {
                $this->setWarning("MSG203", "分納データが存在します。\\n\\n学籍番号：".$schregno);
                Query::dbCheckIn($db);
                return;
            }
            //小分類編集時
            if ($this->div == "S") {

                //親費目(中分類)の入金済みデータチェック
                $query = knjp070kQuery::CheckPaidData("M", $schregno, $m_cd);
                if (is_array($db->getRow($query))) {
                    $this->setWarning("MSG203", "入金済データが存在します。\\n\\n学籍番号：".$schregno);
                    Query::dbCheckIn($db);
                    return;
                }
            }

            //データが存在するデータに対してのみ更新
            $query = knjp070kQuery::CheckExistData($this->div, $schregno, $this->field["EXPENSE_".$this->div."_CD"]);

            if (is_array($db->getRow($query))) {
                $query = knjp070kQuery::getUpdateQuery($this->div, $this->field, $schregno);
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        $this->setMessage("MSG201");
    }

    //一括削除
    function &getAllDeleteModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = explode(",", $this->selectdata);

        if ($this->div == "M") {
            $m_cd = $this->field["EXPENSE_".$this->div."_CD"];
        } else {
            $m_cd = $db->getOne(knjp070kQuery::getCheckMcd($this->field["EXPENSE_".$this->div."_CD"]));
        }

        foreach ($data as $schregno)
        {
            //入金済データチェック
            if ($this->div == "M") {
                $query = knjp070kQuery::CheckPaidData($this->div, $schregno, $this->field["EXPENSE_".$this->div."_CD"]);
            } else {
                $query = knjp070kQuery::CheckPaidSdata($schregno, $this->field["EXPENSE_".$this->div."_CD"]);
            }
            if (is_array($db->getRow($query))) {
                $this->setWarning("MSG203", "入金済データが存在します。\\n\\n学籍番号：".$schregno);
                $db->rollback();
                Query::dbCheckIn($db);
                return;
            }
            //分納データ存在チェック
            $query = knjp070kQuery::CheckInstData($schregno, $m_cd);
            if (is_array($db->getRow($query))) {
                $this->setWarning("MSG203", "分納データが存在します。\\n\\n学籍番号：".$schregno);
                Query::dbCheckIn($db);
                return;
            }
            //小分類編集時
            if ($this->div == "S") {

                //親費目(中分類)の入金済みデータチェック
                $query = knjp070kQuery::CheckPaidData("M", $schregno, $m_cd);
                if (is_array($db->getRow($query))) {
                    $this->setWarning("MSG203", "入金済データが存在します。\\n\\n学籍番号：".$schregno);
                    Query::dbCheckIn($db);
                    return;
                }
            }

            $query = knjp070kQuery::getDeleteQuery($this->div, $this->field, $schregno);
            $db->query($query);

            //下位の小分類を削除
            if ($this->div == "M") {
                $query = knjp070kQuery::getDeleteQuery2($this->field, $schregno);
                $db->query($query);
            }
        }
        $db->commit();
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);
    }

    //一括追加
    function &getAllAddModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data = explode(",", $this->selectdata);

        if ($this->div == "S") {
            $m_cd = $db->getOne(knjp070kQuery::getCheckMcd($this->field["EXPENSE_".$this->div."_CD"]));
        }

        foreach ($data as $schregno)
        {
            //小分類編集時
            if ($this->div == "S") {
                //親費目(中分類)の分納データ存在チェック
                $query = knjp070kQuery::CheckInstData($schregno, $m_cd);
                if (is_array($db->getRow($query))) {
                   $this->setWarning("MSG203", "分納データが存在します。\\n\\n学籍番号：".$schregno);
                   Query::dbCheckIn($db);
                   return;
                }
                $sex_cd = $db->getOne(knjp070kQuery::getCheckMcdSex($this->field["EXPENSE_".$this->div."_CD"]));
                $base_sex = $db->getOne(knjp070kQuery::getCheckBaseSex($schregno));

                if ($sex_cd != "" && $sex_cd != $base_sex) {
                    $this->setWarning("MSG203", "この項目の性別対象ではありません。");
                    Query::dbCheckIn($db);
                    return;
                }

                //親費目(中分類)の入金済みデータチェック
                $query = knjp070kQuery::CheckPaidData("M", $schregno, $m_cd);
                if (is_array($db->getRow($query))) {
                    $this->setWarning("MSG203", "入金済データが存在します。\\n\\n学籍番号：".$schregno);
                    Query::dbCheckIn($db);
                    return;
                }
                $query = knjp070kQuery::CheckSdata($this->div, $schregno, $m_cd);
                if (!is_array($db->getRow($query))) {
                    $this->setWarning("MSG203", "登録されていない生活行事費です。\\n\\n学籍番号：".$schregno);
                    Query::dbCheckIn($db);
                    return;
                }
            } else {
                $l_cd = $db->getOne(knjp070kQuery::getCheckLcd($this->field["EXPENSE_".$this->div."_CD"]));
                $query = knjp070kQuery::Check03Mdata($this->div, $schregno);
                if ($l_cd == "03" && is_array($db->getRow($query))) {
                    $this->setWarning("MSG203", "生活行事費の登録は１つまでです。\\n\\n学籍番号：".$schregno);
                    Query::dbCheckIn($db);
                    return;
                }
            }

            //データが存在しないデータに対してのみ追加
            $query = knjp070kQuery::CheckExistData($this->div, $schregno, $this->field["EXPENSE_".$this->div."_CD"]);

            if (!is_array($db->getRow($query))) {
                $query = knjp070kQuery::getAddQuery($this->div, $this->field, $schregno);
                $db->query($query);
            }
        }
        $db->commit();
        $this->setMessage("MSG201");
        Query::dbCheckIn($db);
    }
}
?>
