<?php

require_once('for_php7.php');


class knjl385jModel extends model
{
    var $cmd;
    var $ObjYear;
    var $pre_hr_class;
    var $classDiv;

    function knjl385jModel() {
        $this->ObjYear     = CTRL_YEAR+1;
    }

    function init() {
        $this->cmd    = VARS::request("cmd");
        $this->pre_hr_class = strlen(VARS::post("PRE_HR_CLASS")) ? VARS::post("PRE_HR_CLASS") : VARS::post("HID_PRE_HR_CLASS");
        $this->classDiv = VARS::post("CLASSDIV");
    }

    function &getMainModel() {
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel() {
        //データ項目名を配列にする。
        $term = array("ATTENDFLG1","ATTENDFLG2","SCORE1","SCORE2","STATIONCD1","STATIONCD2","STATIONCD3","STATIONCD4","PRE_INFO1","PRE_INFO2","PRE_INFO3","REMARK");
        //初期化
        $this->fields = array();
        for ($j=0; $j < get_count($term); $j++) {
            for($i=0;$i<get_count($this->data["EXAMNO"]);$i++){
                $term_name = $term[$j]."-".$i;
                $post_term = VARS::post($term_name);
                if (isset($post_term)) {
                    $this->fields[$term[$j]][] = $post_term;
                } else {
                    $this->fields[$term[$j]][] = "";
                }
            }
        }
        $result = knjl385jQuery::getUpdateQuery($this);
        if ($result) {
            $this->setMessage("MSG201");
        }
    }
}
?>
