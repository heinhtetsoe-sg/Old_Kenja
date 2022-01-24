<?php

require_once('for_php7.php');

class knjd621cModel extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $selectdata;

    //メンバ変数の初期化
    function knjd621cModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");

        $this->field = array("YEAR"         =>  VARS::post("YEAR"),         //年度
                             "SEMESTER"     =>  VARS::post("SEMESTER"),     //学期
                             "GRADE"        =>  VARS::post("GRADE"),        //学年
                             "DATA_DIV"     =>  VARS::post("DATA_DIV"),     //データ種別
                             "MOCKCD"       =>  VARS::post("MOCKCD"),       //テスト名称
                             "RANK_RANGE"   =>  VARS::post("RANK_RANGE"),   //上位順位指定
                             "ASSESS_LEVEL" =>  VARS::post("ASSESS_LEVEL"), //段階値出力
                             "DBNAME"       =>  VARS::post("DBNAME")        //DB変更に備えて
                            );

        $this->selectdata = VARS::post("selectdata");
    }

    function clean()
    {
    }

    //出欠状況不振者
    function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ
        $assess_header = ($this->field["ASSESS_LEVEL"] == "1") ? "段階値" : "";
        $header = array("GRADE_RANK"    => "順位",
                        "NAME"          => "氏名",
                        "HR_NAME"       => "クラス",
                        "SCORE"         => "得点",
                        "ASSESS_LEVEL"  => $assess_header
                        );

        //初期化
        $contents = "";
        $data = false;

        //変数
        $wrk_nendo = common::DateConv1(str_replace("-", "/", CTRL_DATE), 10);
        $gradeName = $db->getOne(knjd621cQuery::getGrade($this->field["GRADE"]));
        $testName = $db->getOne(knjd621cQuery::getMockcd($this->field["DATA_DIV"], $this->field["MOCKCD"]));
        $subclasscd = explode (",", $this->selectdata);

        //セット
        if ($subclasscd[0] != "") {
            for ($i = 0; $i < get_count($subclasscd); $i++){
                //タイトル行
                $subclassName = ($subclasscd[$i] == '999999') ? '合計' : $db->getOne(knjd621cQuery::getSubclassMst($subclasscd[$i]));
                $title = "得点上位者一覧";
                $contents .= $wrk_nendo ."　".$gradeName."　".$testName.$title."（".$subclassName."）\n\n";
                //ヘッダ行
                $contents .= implode($header, ",") ."\n";
                //明細行
                $query = knjd621cQuery::selectCsvQuery($this, $subclasscd[$i]);
                $result = $db->query($query);
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    $row = str_replace(",",".",$row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $contents .= implode($row, ",") ."\n";
                    $data = true;
                }
                $result->free();
                $contents .= "\n\n";
            }
        }

        //DB切断
        Query::dbCheckIn($db);

        //出力
        if ($data){
            $couse = "得点上位者一覧";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        }else{
            $this->setWarning("MSG303");
            return false;
        }

    }

}
?>
