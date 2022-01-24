<?php

require_once('for_php7.php');

//メニュー画面
class knjl115kModel extends model
{
    var $cmd;                   //一覧表示時のcmd
    var $year;
    var $field;
    var $endNumber;

    function knjl115kModel()
    {
        $this->year         = CTRL_YEAR + 1;
    }
    function init()
    {
        $this->cmd          = VARS::request("cmd");

        $this->field = array("TESTDIV"      => VARS::post("TESTDIV"),
                             "OUTPUT"       => VARS::post("OUTPUT"),
                             "SUC_RADIO"    => VARS::post("SUC_RADIO"),
                             "PASSCOUNT"    => VARS::post("PASSCOUNT"),
                             "STARTNUMBER"  => VARS::post("STARTNUMBER"),
                             "YEAR"         => $this->year
                            );
    }
    //入力項目チェック
    function validateInput()
    {
        //終了番号取得
        knjl115kQuery::setEndNumber($this);

        //番号オーバーチェック
        if($this->endNumber > 9999){
            $this->setWarning("MSG915","（開始番号）");
            return false;
        }

        //合格通知No8000オーバーチェック
        if($this->jhflg != "0" && $this->field["OUTPUT"] == '1'){
            if($this->endNumber > 8000){
                $this->setWarning("MSG915","(開始番号)");
                return false;
            }
        }

        $db = Query::dbCheckOut();

        //対象存在チェック
        $query = " SELECT COUNT(*) AS COUNT FROM ENTEXAM_APPLICANTBASE_DAT ".knjl115kQuery::getTargetNumber($this);
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if($Row["COUNT"] <= 0){
            $this->setWarning("MSG303");
            Query::dbCheckIn($db);
            return false;
        }

        //対象データ採番済チェック2006/01/12
        $where = knjl115kQuery::getTargetNumber($this);
        $query = " SELECT COUNT(*) AS COUNT FROM ENTEXAM_APPLICANTBASE_DAT ";

        if ($this->field["OUTPUT"] == '1' && $this->field["SUC_RADIO"] != '12'){         #合格者

            $query .= $where;
            $query .= " AND SUCCESS_NOTICENO IS NOT NULL ";

        }else if ($this->field["OUTPUT"] == '1' && $this->field["SUC_RADIO"] == '12'){   #合格者 追加繰上

            $query .= $where;
            $query .= "  AND ((judgement ='5' AND SUCCESS_NOTICENO IS NOT NULL )";
            $query .= "   OR  (judgement= '6' AND SUCCESS_NOTICENO IS NOT NULL AND OLD_SUCCESS_NOTICENO IS NOT NULL) )";

        }else if ($this->field["OUTPUT"] == '2'){   #不合格者

            $query .= $where;
            $query .= " AND FAILURE_NOTICENO IS NOT NULL ";

        }
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if(0 < $Row["COUNT"]){
            $this->setWarning("MSG307");
            Query::dbCheckIn($db);
            return false;
        }

        //重複チェック
        $query = knjl115kQuery::getDuplicationCheck($this);
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if($Row["COUNT"] > 0){
            $this->setWarning("MSG302");
            Query::dbCheckIn($db);
            return false;
        }

        //追加繰上合格者存在チェック
        if ($this->field["SUC_RADIO"] == '11'){         #一般
            $query = knjl115kQuery::GetCountAdvance($this->year, $this->field["TESTDIV"]);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            if($Row["COUNT"] > 0){
                $this->setWarning("MSG203","(追加繰上合格者が存在します。)");
                Query::dbCheckIn($db);
                return false;
            }
        }

        Query::dbCheckIn($db);
        return true;
    }

    /**
     * メイン表示用のモデルを返します
    */
    function &getMainModel()
    {
    }

    // 確定のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            knjl115kQuery::updateChainNumber($this);
            $this->setMessage("MSG201");
        }
    }

    // クリアのモデルを返します #2005/12/30
    function &getClearModel()
    {
        if ($this->field["OUTPUT"] == '1'){         #合格者
            if ($this->field["SUC_RADIO"] == '11'){         #一般
                knjl115kQuery::ClearNumber0111($this);
            }else if ($this->field["SUC_RADIO"] == '12'){   #追加繰上
                knjl115kQuery::ClearNumber0112($this);
            }else if ($this->field["SUC_RADIO"] == '13'){   #附属推薦
                knjl115kQuery::ClearNumber0113($this);
            }else{
                $this->setWarning("MSG901","（合格区分）");
            }
        }else if ($this->field["OUTPUT"] == '2'){   #不合格者
            knjl115kQuery::ClearNumber0200($this);
        }else{
            $this->setWarning("MSG901","（合否区分）");
            return false;
        }

        $this->setMessage("MSG201");
    }

}
?>
