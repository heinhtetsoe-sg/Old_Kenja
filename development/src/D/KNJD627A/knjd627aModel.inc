<?php

require_once('for_php7.php');

class knjd627aModel extends Model
{
    public $grade;
    public $kind;
    public $cmd;
    public $control;

    public function knjd627aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    
    public function init()
    {
        $this->cmd      = VARS::request("cmd");
        $this->grade    = VARS::post("GRADE");
        //プロパティファイル
        $this->getPropertiesAll();
    }

    public function &getMainModel()
    {
    }

    //事前処理チェック
    public function validateInput()
    {
        $db = Query::dbCheckOut();

        //存在チェック（在籍データ）
        $query = knjd627aQuery::checkToSchregno($this);
        $cnt_sch = $db->getOne($query);
        if (0 < $cnt_sch) {
        } else {
            $this->setWarning("MSG303", "\\n(学籍在籍データ)\\n学年または組またはコースが設定されているか確認して下さい。");
            return false;
        }

        //存在チェック（成績データ）
        $query = knjd627aQuery::checkToRecordScore($this);
        $cnt_rec = $db->getOne($query);

        if (0 < $cnt_rec) {
        } else {
            $this->setWarning("MSG303", "\\n(成績データ)");
            return false;
        }

        Query::dbCheckIn($db);

        return true;
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        //事前処理チェック
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $query = knjd627aQuery::getSoutenData($this);
            $result = $db->query($query);
            $upddata = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $upddata[] = $row;
            }
            Query::dbCheckIn($db);
            if (knjd627aQuery::getUpdateQuery($this, $upddata)) {
                $this->setMessage("MSG202");
            }
        }
    }
}
