<?php

require_once('for_php7.php');


class knjd910Model extends Model {
    var $seme;

    function knjd910Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }
    
    function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        $this->cmd      = VARS::request("cmd");
        $this->seme     = VARS::post("SEMESTER");
    }

    function &getMainModel()
    {
    }

    //事前処理チェック
    function validateInput()
    {
        //存在チェック（講座名簿データ）
        $cntChair = knjd910Query::existsChairStd($this);
        if (0 < $cntChair) {
        } else {
            $this->setWarning("MSG300", "\\n講座名簿データが１件も存在しないため、処理できません。");
            return false;
        }
        //存在チェック（削除する成績データ）
        $cntRecord = knjd910Query::existsRecordScoreDeleteData($this);
        if (0 < $cntRecord) {
        } else {
            $this->setWarning("MSG303", "\\n削除する成績データが１件も存在していません。");
            return false;
        }

        return true;
    }

    /**
     * 実行モデルを返します
    */
    function &getExecModel()
    {
        //事前処理チェック
        if ($this->validateInput()){
            if (knjd910Query::ExecuteQuery($this)) {
                $this->setMessage("MSG202");
            }
        }
    }
}
?>
