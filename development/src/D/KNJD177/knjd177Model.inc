<?php

require_once('for_php7.php');

class knjd177Model extends Model
{
    public $semester;
    public $hrClass;
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $selectdata;

    public function knjd177Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->semester = VARS::post("SEMESTER");
        $this->hrClass  = VARS::post("GRADE_HR_CLASS");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
        }
    
        $this->field = array("DATE"             =>  VARS::post("DATE"),            //異動対象日付
                             "YEAR"             =>  VARS::post("YEAR"),            //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),        //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),  //年組
                             "ADDR_PRINT"       =>  VARS::post("ADDR_PRINT"),      //住所印字
                             "ADDR_DIV"         =>  VARS::post("ADDR_DIV"),        //住所区分
                             "PRINT_KATEI"      =>  VARS::post("PRINT_KATEI"),     //家庭からの欄
                             "GROUP_DIV"        =>  VARS::post("GROUP_DIV"),       //グループ区分
                             "RANK_DIV"         =>  VARS::post("RANK_DIV"),        //順位の基準点
                             "PATARN_DIV"       =>  VARS::post("PATARN_DIV"),      //帳票パターン
                             "GAKUNEN_HYOUTEI"  =>  VARS::post("GAKUNEN_HYOUTEI"), //帳票パターン
                             "KEKKA_PRINT"      =>  VARS::post("KEKKA_PRINT"),     //欠課時数印字(Ｂパターンのみ)
                             "SHIMEBI_PRINT"    =>  VARS::post("SHIMEBI_PRINT"),   //締め日までの出欠日数を出力する(Ｃ・Ｅパターンのみ)
                             "RANK_PRINT_ALL"   =>  VARS::post("RANK_PRINT_ALL"),  //順位印字(C以外)
                             "RANK_PRINT1"      =>  VARS::post("RANK_PRINT1"),     //順位印字(テスト1)
                             "RANK_PRINT2"      =>  VARS::post("RANK_PRINT2"),     //順位印字(テスト2)
                             "RANK_PRINT3"      =>  VARS::post("RANK_PRINT3"),     //順位印字(テスト3)
                             "RANK_PRINT4"      =>  VARS::post("RANK_PRINT4"),     //順位印字(テスト4)
                             "RANK_PRINT5"      =>  VARS::post("RANK_PRINT5"),     //順位印字(テスト5)
                             "RANK_PRINT6"      =>  VARS::post("RANK_PRINT6"),     //順位印字(テスト6)
                             "RANK_PRINT7"      =>  VARS::post("RANK_PRINT7"),     //順位印字(テスト7)
                             "RANK_PRINT8"      =>  VARS::post("RANK_PRINT8"),     //順位印字(テスト8)
                             "HIDDEN_RANK_PR1NT1"      =>  VARS::post("HIDDEN_RANK_PR1NT1"),     //順位印字(テスト1)
                             "HIDDEN_RANK_PR1NT2"      =>  VARS::post("HIDDEN_RANK_PR1NT2"),     //順位印字(テスト2)
                             "HIDDEN_RANK_PR1NT3"      =>  VARS::post("HIDDEN_RANK_PR1NT3"),     //順位印字(テスト3)
                             "HIDDEN_RANK_PR1NT4"      =>  VARS::post("HIDDEN_RANK_PR1NT4"),     //順位印字(テスト4)
                             "HIDDEN_RANK_PR1NT5"      =>  VARS::post("HIDDEN_RANK_PR1NT5"),     //順位印字(テスト5)
                             "HIDDEN_RANK_PR1NT6"      =>  VARS::post("HIDDEN_RANK_PR1NT6"),     //順位印字(テスト6)
                             "HIDDEN_RANK_PR1NT7"      =>  VARS::post("HIDDEN_RANK_PR1NT7"),     //順位印字(テスト7)
                             "HIDDEN_RANK_PR1NT8"      =>  VARS::post("HIDDEN_RANK_PR1NT8"),     //順位印字(テスト8)
                             "RANK_PRINT_SEM1"  =>  VARS::post("RANK_PRINT_SEM1"), //順位印字(学期成績)
                             "RANK_PRINT_SEM2"  =>  VARS::post("RANK_PRINT_SEM2"), //順位印字(学期成績)
                             "RANK_PRINT_SEM3"  =>  VARS::post("RANK_PRINT_SEM3"), //順位印字(学期成績)
                             "RANK_PRINT_SEM9"  =>  VARS::post("RANK_PRINT_SEM9"), //順位印字(学期成績)
                             "HIDDEN_RANK_PR1NT_SEM1"  =>  VARS::post("HIDDEN_RANK_PR1NT_SEM1"), //順位印字(学期成績)
                             "HIDDEN_RANK_PR1NT_SEM2"  =>  VARS::post("HIDDEN_RANK_PR1NT_SEM2"), //順位印字(学期成績)
                             "HIDDEN_RANK_PR1NT_SEM3"  =>  VARS::post("HIDDEN_RANK_PR1NT_SEM3"), //順位印字(学期成績)
                             "HIDDEN_RANK_PR1NT_SEM9"  =>  VARS::post("HIDDEN_RANK_PR1NT_SEM9"), //順位印字(学期成績)
                             "KYOUKA_SU"        =>  VARS::post("KYOUKA_SU"),       //順位印字(教科数)
                             "DANKAI_YOMIKAE"   =>  VARS::post("DANKAI_YOMIKAE"),  //段階値は読替あり（Ｅパターンのみ）
                             "DBNAME"           =>  VARS::post("DBNAME")           //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }
        
        $this->field["RANK_PRINT1"] = $this->field["HIDDEN_RANK_PR1NT1"] == "" ? "1" : $this->field["HIDDEN_RANK_PR1NT1"];
//        $this->field["RANK_PRINT2"] = $this->field["HIDDEN_RANK_PR1NT2"] == "" ? "1" : $this->field["HIDDEN_RANK_PR1NT2"];
//        $this->field["RANK_PRINT3"] = $this->field["HIDDEN_RANK_PR1NT3"] == "" ? "1" : $this->field["HIDDEN_RANK_PR1NT3"];
//        $this->field["RANK_PRINT4"] = $this->field["HIDDEN_RANK_PR1NT4"] == "" ? "1" : $this->field["HIDDEN_RANK_PR1NT4"];
//        $this->field["RANK_PRINT5"] = $this->field["HIDDEN_RANK_PR1NT5"] == "" ? "1" : $this->field["HIDDEN_RANK_PR1NT5"];
//        $this->field["RANK_PRINT6"] = $this->field["HIDDEN_RANK_PR1NT6"] == "" ? "1" : $this->field["HIDDEN_RANK_PR1NT6"];
//        $this->field["RANK_PRINT7"] = $this->field["HIDDEN_RANK_PR1NT7"] == "" ? "1" : $this->field["HIDDEN_RANK_PR1NT7"];
//        $this->field["RANK_PRINT8"] = $this->field["HIDDEN_RANK_PR1NT8"] == "" ? "1" : $this->field["HIDDEN_RANK_PR1NT8"];
//        $this->field["RANK_PRINT_SEM1"] = $this->field["HIDDEN_RANK_PR1NT_SEM1"] == "" ? "1" : $this->field["HIDDEN_RANK_PR1NT_SEM1"];
//        $this->field["RANK_PRINT_SEM2"] = $this->field["HIDDEN_RANK_PR1NT_SEM2"] == "" ? "1" : $this->field["HIDDEN_RANK_PR1NT_SEM2"];
//        $this->field["RANK_PRINT_SEM3"] = $this->field["HIDDEN_RANK_PR1NT_SEM3"] == "" ? "1" : $this->field["HIDDEN_RANK_PR1NT_SEM3"];
//        $this->field["RANK_PRINT_SEM9"] = $this->field["HIDDEN_RANK_PR1NT_SEM9"] == "" ? "1" : $this->field["HIDDEN_RANK_PR1NT_SEM9"];

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
