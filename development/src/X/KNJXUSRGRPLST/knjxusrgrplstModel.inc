<?php
class knjxusrgrplstModel extends Model {
    var $cmd;
    var $s_id;
    var $sort;

    function knjxusrgrplstModel()
    {
    }

    function init()
    {
        $this->cmd          = VARS::request("cmd");
        //起動時のデフォルトでソートを"職員コード＋グループコード"の昇順にする
        $this->s_id = "1";
        $this->sort["1"] = "1";
        $this->sort["2"] = "0";
        $this->sort["3"] = "0";
        $this->sort["4"] = "0";

        //共通関数(コントロールマスタより)
        common::GetControlMaster_Fnc($this->ctrl);

        if (VARS::get("PROGRAMID") != "") {
            $this->s_id = "1";
            $this->sort["1"] = "1";
            $this->sort["2"] = "0";
            $this->sort["3"] = "0";
            $this->sort["4"] = "0";
            return;
        }
        elseif (VARS::get("sort1") != "" || VARS::get("sort2") != "" || VARS::get("sort3") != ""|| VARS::get("sort4") != "") {
            $this->sort["1"] = (VARS::get("sort1") != "" ) ? VARS::get("sort1") : "0";
            $this->sort["2"] = (VARS::get("sort2") != "" ) ? VARS::get("sort2") : "0";
            $this->sort["3"] = (VARS::get("sort3") != "" ) ? VARS::get("sort3") : "0";
            $this->sort["4"] = (VARS::get("sort4") != "" ) ? VARS::get("sort4") : "0";
        }
        if (VARS::get("s_id") != ""){
            $this->s_id     = VARS::get("s_id");
        }
        $this->no           = VARS::get("NO");

        //プロパティファイル
        $this->getPropertiesAll();
    }
}
?>
