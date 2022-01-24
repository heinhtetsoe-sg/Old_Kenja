<?php

require_once('for_php7.php');

class knjd179Model extends Model {
    var $semester;
    var $hrClass;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ
    var $tutisyoTokubetuKatudo;

    function knjd179Model() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        $this->hrClass  = VARS::post("GRADE_HR_CLASS");
        $this->test_cd  = VARS::post("TEST_CD");
        $this->soten_hyouka_hyoutei  = VARS::post("SOTEN_HYOUKA_HYOUTEI");
        $this->cmd      = VARS::request("cmd");

        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "TYUI_TYOUKA"      =>  VARS::post("TYUI_TYOUKA"),      //注意・超過ラジオボタン
                             "DATE_DIV"         =>  VARS::post("DATE_DIV")          //出欠集計範囲（累計・学期）ラジオボタン
                             );
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
            unset($this->test_cd);
            unset($this->soten_hyouka_hyoutei);
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }
        if (!$this->soten_hyouka_hyoutei) {
            $this->soten_hyouka_hyoutei = '1';
        }
        $this->getSemesterDetails();

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->tutisyoTokubetuKatudo = (strlen($this->Properties["tutisyoTokubetuKatudo"])) ? $this->Properties["tutisyoTokubetuKatudo"] : 0;
    }

    //各学校のテストコードと学期詳細コード
    function getSemesterDetails() {
        /**********************/
        /* 出欠集計開始日付用 */
        /**********************/
        /* アンダーバーの後ろは切替コード */
        //湖陵
        $this->semesterDetailS["220170"] = array("10101" => "1",
                                                 "19900" => "1",
                                                 "20101" => "3",
                                                 "29900" => "3",
                                                 "99900" => "1"
                                            );
        //倉吉
        $this->semesterDetailS["220360"] = array("10101" => "1",
                                                 "19900" => "1",
                                                 "20101" => "3",
                                                 "29900" => "3",
                                                 "99900" => "1"
                                            );
        //米子
        $this->semesterDetailS["220250"] = array("10101" => "1",
                                                 "10201" => "1",
                                                 "19900" => "1",
                                                 "20101" => "4",
                                                 "20201" => "4",
                                                 "20202" => "4",
                                                 "99900" => "1"
                                            );
        //境
        $this->semesterDetailS["220410"] = array("10101" => "1",
                                                 "10201" => "1",
                                                 "19900" => "1",
                                                 "20101" => "4",
                                                 "20201" => "4",
                                                 "20202" => "4",
                                                 "99900" => "1"
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
}
?>
