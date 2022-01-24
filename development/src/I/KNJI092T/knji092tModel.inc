<?php

require_once('for_php7.php');

class knji092tModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ

    function knji092tModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"               =>  VARS::post("YEAR"),                 //年度
                            "SCHOOL_KIND"         =>  VARS::post("SCHOOL_KIND"),          //校種
                            "MIKOMI"              =>  VARS::post("MIKOMI"),               //卒業見込み出力
                            "WRITE_DIV"           =>  VARS::post("WRITE_DIV"),            //縦書き/横書き
                            "ENT_DATE"            =>  VARS::post("ENT_DATE"),             //入学年月日
                            "OUTPUT1"             =>  VARS::post("OUTPUT1"),              //表紙
                            "OUTPUT2"             =>  VARS::post("OUTPUT2"),              //名簿
                            "OUTPUT3"             =>  VARS::post("OUTPUT3"),              //性別
                            "GAKKI"               =>  VARS::post("GAKKI"),                //学期コード
                            "CLASS_MAJOR"         =>  VARS::post("CLASS_MAJOR"),          //クラス・学科
                            "MAJOR_ORDER"         =>  VARS::post("MAJOR_ORDER"),          //学科指定時の五十音順
                            "ENT_GRD_DATE_FORMAT" =>  VARS::post("ENT_GRD_DATE_FORMAT"),  //入学・卒業日付は年月で表示する
                            "PATTERN"             =>  VARS::post("PATTERN"),              //パターン
                            "DBNAME"              =>  VARS::post("DBNAME")                //DB変更に備えて
                            );


        $this->cmd  = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->useSchregRegdHdat = (strlen($this->Properties["useSchregRegdHdat"])) ? $this->Properties["useSchregRegdHdat"] : 0;
    }

    function clean() {}
}
?>
