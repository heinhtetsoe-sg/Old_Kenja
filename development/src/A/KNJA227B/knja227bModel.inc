<?php

require_once('for_php7.php');


class knja227bModel extends Model {
    var $field      = array();
    var $cmd;

    function knja227bModel() {
    }

    function init() {
        $this->field = array("SEARCH_PRISCHOOLCD"           =>  VARS::post("SEARCH_PRISCHOOLCD"),           //塾コード
                             "SEARCH_PRISCHOOL_NAME"        =>  VARS::post("SEARCH_PRISCHOOL_NAME"),        //塾名
                             "SEARCH_PRISCHOOL_CLASS_NAME"  =>  VARS::post("SEARCH_PRISCHOOL_CLASS_NAME"),  //教室名
                             "BRIEFING_DATE"                =>  VARS::post("BRIEFING_DATE"),                //説明会(日)
                             "BRIEFING_AMPM"                =>  VARS::post("BRIEFING_AMPM"),                //説明会(午前、午後)
                             "BRIEFING_HOUR"                =>  VARS::post("BRIEFING_HOUR"),                //説明会(時)
                             "BRIEFING_MINUTE"              =>  VARS::post("BRIEFING_MINUTE"),              //説明会(分)
                             "SEND_DATE"                    =>  VARS::post("SEND_DATE"),                    //資料送付日
                             "DBNAME"                       =>  VARS::post("DBNAME")                        //DB変更に備えて
                     );

        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
