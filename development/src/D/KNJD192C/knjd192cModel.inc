<?php

require_once('for_php7.php');

class knjd192cModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $semesterDetailS;
    var $semesterDetailE;

    function knjd192cModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コード
                             "GRADE"                =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"             =>  VARS::post("HR_CLASS"),             //組
                             "SUB_TESTCD"           =>  VARS::post("SUB_TESTCD"),           //テスト
                             "CATEGORY_IS_CLASS"    =>  VARS::post("CATEGORY_IS_CLASS"),    //表示区分
                             "GROUP_DIV"            =>  VARS::post("GROUP_DIV"),            //平均・席次
                             "OUTPUT_KIJUN"         =>  VARS::post("OUTPUT_KIJUN"),         //順位の基準点
                             "COUNT_SURU"           =>  VARS::post("COUNT_SURU"),           //欠席をカウントするかのフラグ
                             "KETTEN"               =>  VARS::post("KETTEN"),               //欠点
                             "SUBCLASS_MAX"         =>  VARS::post("SUBCLASS_MAX"),         //最大科目数
                             "TEST_ONLY"            =>  VARS::post("TEST_ONLY"),            //試験科目のみ出力する
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();

        $this->getSemesterDetails();
    }

    //各学校のテストコードと学期詳細コード
    function getSemesterDetails() {
        /**********************/
        /* 出欠集計開始日付用 */
        /**********************/
        /* アンダーバーの後ろは切替コード */
        //湖陵
        $this->semesterDetailS["220170"] = array("10101_2" => "1",
                                                 "19900_2" => "1",
                                                 "20101_2" => "3",
                                                 "29900_2" => "3",
                                                 "99900_2" => "1"
                                            );
        //倉吉
        $this->semesterDetailS["220360"] = array("10101_1" => "1",
                                                 "19900_2" => "1",
                                                 "20101_1" => "3",
                                                 "29900_2" => "3",
                                                 "99900_2" => "1"
                                            );
        //米子
        $this->semesterDetailS["220250"] = array("10101_1" => "1",
                                                 "10201_1" => "1",
                                                 "10201_2" => "1",
                                                 "19900_2" => "1",
                                                 "20101_1" => "4",
                                                 "20201_1" => "4",
                                                 "20201_2" => "4",
                                                 "20202_1" => "4",
                                                 "99900_2" => "1"
                                            );
        //境
        $this->semesterDetailS["220410"] = array("10101_1" => "1",
                                                 "10201_1" => "1",
                                                 "10201_2" => "1",
                                                 "19900_2" => "1",
                                                 "20101_1" => "4",
                                                 "20201_1" => "4",
                                                 "20201_2" => "4",
                                                 "20202_1" => "4",
                                                 "99900_1" => "1",
                                                 "99900_2" => "1"
                                            );

        /**********************/
        /* 出欠集計終了日付用 */
        /**********************/
        //湖陵
        $this->semesterDetailE["220170"] = array("10101" => "1",
                                                "19900" => "2",
                                                "20101" => "3",
                                                "29900" => "4",
                                                "99900" => "5"
                                            );
        //倉吉
        $this->semesterDetailE["220360"] = array("10101" => "1",
                                                "19900" => "2",
                                                "20101" => "3",
                                                "29900" => "4",
                                                "99900" => "5"
                                            );
        //米子
        $this->semesterDetailE["220250"] = array("10101" => "1",
                                                "10201" => "2",
                                                "19900" => "3",
                                                "20101" => "4",
                                                "20201" => "5",
                                                "20202" => "6",
                                                "99900" => "6"
                                            );
        //境
        $this->semesterDetailE["220410"] = array("10101" => "1",
                                                "10201" => "2",
                                                "19900" => "3",
                                                "20101" => "4",
                                                "20201" => "5",
                                                "20202" => "6",
                                                "99900" => "6"
                                            );
    }

    function clean() {}
}
?>
