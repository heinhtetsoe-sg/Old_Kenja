<?php

require_once('for_php7.php');

class knjd154vModel extends Model {
    var $semester;
    var $hrClass;
    var $selectdata;
    var $selectdata2;
    var $cmd;
    var $control;        //コントロールマスタ
    var $tutisyoTokubetuKatudo;

    function knjd154vModel() {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-","/",$this->control["学籍処理日"]);
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        $this->hrClass  = VARS::post("GRADE_HR_CLASS");
        $this->test_cd  = VARS::post("TEST_CD");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
            unset($this->test_cd);
        }

        $this->field = array("SEMESTER"                    =>  VARS::post("SEMESTER"),                     //学期
                             "AVG_PRINT"                   =>  VARS::post("AVG_PRINT"),                    //平均値出力選択チェックボックス
                             "AVG_DIV"                     =>  VARS::post("AVG_DIV"),                      //平均値ラジオボタン
                             "NO_PRINT_STUDENT_NAME"       =>  VARS::post("NO_PRINT_STUDENT_NAME"),        //生徒名出力なし
                             "NO_PRINT_STUDENT_NAME2"      =>  VARS::post("NO_PRINT_STUDENT_NAME2"),       //生徒名出力なし2
                             "OKURIJOU_JUSYO"              =>  VARS::post("OKURIJOU_JUSYO"),               //送り状住所ラジオボタン
                             "TYUI_TYOUKA"                 =>  VARS::post("TYUI_TYOUKA"),                  //注意・超過ラジオボタン
                             "JUSYO_PRINT"                 =>  VARS::post("JUSYO_PRINT"),                  //送り状住所なしチェックボックス
                             "DATE_DIV"                    =>  VARS::post("DATE_DIV"),                     //出欠集計範囲（累計・学期）ラジオボタン
                             "SHR_KESSEKI_NASI"            =>  VARS::post("SHR_KESSEKI_NASI"),             //ＳＨＲの欠席時数なし チェックボックス
                             "GYOJI_KESSEKI_NASI"          =>  VARS::post("GYOJI_KESSEKI_NASI"),           //学校行事の欠席時数なし チェックボックス
                             "HANREI_SYUTURYOKU_NASI"      =>  VARS::post("HANREI_SYUTURYOKU_NASI"),       //凡例出力なし チェックボックス
                             "KOKETSU_KIBIKI_SHUTTEI_NASI" =>  VARS::post("KOKETSU_KIBIKI_SHUTTEI_NASI"),  //公欠・忌引・出停時数なし チェックボックス
                             "PRINT_NENKAN"                =>  VARS::post("PRINT_NENKAN")                  //年間通算を出力
                             );
        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }
}
?>
