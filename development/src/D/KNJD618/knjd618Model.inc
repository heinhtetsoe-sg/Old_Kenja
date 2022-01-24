<?php

require_once('for_php7.php');

class knjd618Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $semesterDetailS;
    var $semesterDetailE;

    function knjd618Model() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"               =>  VARS::post("YEAR"),              //年度
                             "SEMESTER"           =>  VARS::post("SEMESTER"),          //学期コード
                             "GRADE"              =>  VARS::post("GRADE"),             //学年
                             "HR_CLASS"           =>  VARS::post("HR_CLASS"),          //組
                             "TESTCD"             =>  VARS::post("TESTCD"),            //テスト
                             "DATE_DIV"           =>  VARS::post("DATE_DIV"),          //出欠集計範囲(累計･学期)ラジオボタン
                             "CATEGORY_IS_CLASS"  =>  VARS::post("CATEGORY_IS_CLASS"), //表示区分
                             "GROUP_DIV"          =>  VARS::post("GROUP_DIV"),         //グループ区分
                             "TYUI_TYOUKA"        =>  VARS::post("TYUI_TYOUKA"),       //注意・超過ラジオボタン
                             "DBNAME"             =>  VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->cmd = VARS::request("cmd");
        $this->getSemesterDetails();
        //プロパティファイル
        $this->getPropertiesAll();
    }

    //各学校のテストコードと学期詳細コード
    function getSemesterDetails() {
        /**********************/
        /* 出欠集計開始日付用 */
        /**********************/
        /* アンダーバーの後ろは切替コード */
        //中央育英
        $this->semesterDetailS["224030"] = array("10101" => "1",
                                                 "10201" => "2",
                                                 "10202" => "3",
                                                 "19900" => "3",
                                                 "20101" => "4",
                                                 "20201" => "5",
                                                 "20202" => "6",
                                                 "29900" => "6"
                                            );

        /**********************/
        /* 出欠集計終了日付用 */
        /**********************/
        //中央育英
        $this->semesterDetailE["224030"] = array("10101" => "1",
                                                 "10201" => "2",
                                                 "10202" => "3",
                                                 "19900" => "3",
                                                 "20101" => "4",
                                                 "20201" => "5",
                                                 "20202" => "6",
                                                 "29900" => "6"
                                            );
    }

    function clean() {}
}
?>
