<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjd621Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $selectdata;

    //メンバ変数の初期化
    function knjd621Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル

        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER"),     //学期
                             "GRADE"        =>  VARS::post("GRADE"),        //学年
                             "TESTCD"       =>  VARS::post("TESTCD"),       //テスト
                             "RANK_RANGE"   =>  VARS::post("RANK_RANGE"),   //上位順位
                             "STANDARD"     =>  VARS::post("STANDARD"),     //上位順位
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->selectdata        = VARS::post("selectdata");
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    //出欠状況不振者
    function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダの作成//
        if ($this->field["STANDARD"] == "2") {
            $header = array("GRADE_RANK"    => "順位",
                            "NAME"          => "氏名",
                            "HR_NAME"       => "クラス",
                            "SCORE"         => "平均点"
                            );
        } else {
            $header = array("GRADE_RANK"    => "順位",
                            "NAME"          => "氏名",
                            "HR_NAME"       => "クラス",
                            "SCORE"         => "得点"
                            );
        }

        $contents = "";

        $wrk_nendo = common::DateConv1(str_replace("-", "/", CTRL_DATE), 10);
        $grade = $db->getOne(knjd621Query::getSelectGrade($this, $this->field["GRADE"]));
        $testname = $db->getOne(knjd621Query::getTestcd($this, $this->field["TESTCD"]));
        $subclasscd = explode (",", $this->selectdata);

        if ($subclasscd[0] !="") {
            for ($i = 0; $i < get_count($subclasscd); $i++){

                $subclassname = ($subclasscd[$i] == '999999') ? '合計' : $db->getOne(knjd621Query::getSubclassMst($this, $subclasscd[$i]));

                $title = "得点上位者一覧";
                $contents .= $wrk_nendo ."　".$grade."　".$testname.$title."（".$subclassname."）\n\n";

                $contents .= implode($header, ",") ."\n";

                $query = knjd621Query::selectCsvQuery($this, $subclasscd[$i]);
                $result = $db->query($query);
                $data = false;
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    if ($this->field["STANDARD"] == "2" && $subclasscd[$i] == '999999') { //四捨五入をしたときに後ろの0000をカットする
                        $row["AVG"] = substr($row["AVG"], 0, -4);
                    }
                    $contents .= implode($row, ",") ."\n";
                    $data = true;
                }
                $result->free();
                $contents .= "\n\n";
            }
        }

        Query::dbCheckIn($db);

        if ($data){
            $couse = "得点上位者一覧";
            $filename = sprintf("%s.csv",
                                         $couse
                                );
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }

}
?>
