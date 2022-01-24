<?php

require_once('for_php7.php');

class knjd421Model extends Model {
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ

    function knjd421Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        $this->cmd      = VARS::request("cmd");

        $this->field = array("SEMESTER"               =>  VARS::post("SEMESTER"),               //学期
                             "SCHOOL_KIND"            =>  VARS::post("SCHOOL_KIND"),            //学校種別
                             "HUKUSIKI_RADIO"         =>  VARS::post("HUKUSIKI_RADIO"),         //コンボ切替
                             "GRADE_MIX"              =>  VARS::post("GRADE_MIX"),              //学年混合
                             "GRADE_KIND_GROUP"       =>  VARS::post("GRADE_KIND_GROUP"),       //状態区分グループ
                             "GHR_CD"                 =>  VARS::post("GHR_CD"),                 //年組
                             "selectdata"             =>  VARS::post("selectdata"),             //出力教科区分
                             "DATE"                   =>  VARS::post("DATE"),                   //異動対象日付
                             "DESC_DATE"              =>  VARS::post("DESC_DATE"),              //記載日付
                             "OUTPUT_DIV"             =>  VARS::post("OUTPUT_DIV"),             //通知表 個別の指導計画
                             "PRINT_SIDE1"            =>  VARS::post("PRINT_SIDE1"),            //1.表紙
                             "PRINT_SIDE1_ATTEND"     =>  VARS::post("PRINT_SIDE1_ATTEND"),     //1.表紙 出欠記録
                             "PRINT_SIDE2"            =>  VARS::post("PRINT_SIDE2"),            //2.成績
                             "PRINT_SIDE3"            =>  VARS::post("PRINT_SIDE3"),            //3.出欠
                             "PRINT_SIDE4"            =>  VARS::post("PRINT_SIDE4"),            //4.修了証
                             "OUTPUT_CATEGORY_NAME2"  =>  VARS::post("OUTPUT_CATEGORY_NAME2"),  //教科区分を出力しない
                             "D078"                   =>  VARS::post("D078"),                   //所見学期
                             );

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }
}
?>
