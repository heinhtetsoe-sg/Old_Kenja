<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjl324hModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl324hModel()
    {
    }

    function init()
    {               
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APDIV"            =>  VARS::post("APDIV"),             //入試制度
                             "TESTDV"           =>  VARS::post("TESTDV")             //入試区分
                            );

        $this->cmd         = VARS::request("cmd");
    }
    /**
     * ＣＳＶ出力
     */
    function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行（入試区分・入試区分名称・入試日付を取得）
        $result = $db->query(knjl324hQuery::getNameMst($this));
        $testname = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればカンマに置換・・・置換する文字は、カンマでなければ何でも良い。
            //入試区分名称
            $testname[$row["TEST_CD"]] = $row["TEST_NAME"];
        }

        //ヘッダ
        $school  = "法政大学";
        $school .= ($this->field["APDIV"] == "1") ? "中学校" : "高等学校";
        $title1  = "　入学試験　合格発表";
        $title2  = "入学試験合格者";
        if ($this->field["APDIV"] == "1" || $this->field["APDIV"] == "2") $title2 .= "および繰上合格候補者";
        $title2 .= "の受験番号は以下のとおりです。";
        $title3  = "合格者,,,,";
        if ($this->field["APDIV"] == "1" || $this->field["APDIV"] == "2") $title3 .= "繰上合格候補者";

        //変数の初期値
        $arr_testdiv = $arr_judgediv1 = $arr_judgediv3 = array();

        //SQL文発行（合格者取得）
        $result = $db->query(knjl324hQuery::getNo($this));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row = str_replace(",",".",$row);//データ内にカンマがあればカンマに置換・・・置換する文字は、カンマでなければ何でも良い。
            $data = true;
            $arr_testdiv[$row["TESTDIV"]] = $testname[$row["TESTDIV"]];
            if ($row["JUDGEDIV"] == "1") {
                $arr_judgediv1[$row["TESTDIV"]][] = $row["EXAMNO"];
            } else {
                $arr_judgediv3[$row["TESTDIV"]][] = $row["EXAMNO"];
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        //配列を配置し直す
        $arr_judgediv1_retno = array();
        foreach ($arr_judgediv1 as $key => $val) {
            foreach ($val as $key2 => $val2) {
                $arr_judgediv1_retno[$key][floor($key2 / 40)][] = $val2;
            }
        }
        $arr_judgediv3_retno = array();
        foreach ($arr_judgediv3 as $key => $val) {
            foreach ($val as $key2 => $val2) {
                $arr_judgediv3_retno[$key][floor($key2 / 40)][] = $val2;
            }
        }

        //出力
        $contents = "";
        foreach ($arr_testdiv as $key => $val) {
            //ヘッダ
            $contents .= $school;
            $contents .= "\r\n";
            $contents .= $val .$title1;
            $contents .= "\r\n";
            $contents .= "本校" .$val .$title2;
            $contents .= "\r\n";
            $contents .= $title3;
            $contents .= "\r\n";
            //詳細
            for ($gyo = 0; $gyo < 40; $gyo++) {
                //合格者
                for ($ret = 0; $ret < 4; $ret++) {
                    $contents .= $arr_judgediv1_retno[$key][$ret][$gyo];
                    $contents .= ",";
                }
                //繰上合格候補者
                if ($this->field["APDIV"] == "1" || $this->field["APDIV"] == "2") {
                    for ($ret = 0; $ret < 3; $ret++) {
                        $contents .= $arr_judgediv3_retno[$key][$ret][$gyo];
                        $contents .= ",";
                    }
                }
                $contents .= "\r\n";
            }
            $contents .= ",,,";
            $contents .= "以上" .get_count($arr_judgediv1[$key]) ."名";
            if ($this->field["APDIV"] == "1" || $this->field["APDIV"] == "2") {
                $contents .= ",,,";
                $contents .= "以上" .get_count($arr_judgediv3[$key]) ."名";
            }
            $contents .= "\r\n";
        }

        //対象データ無し
        if (!$data){
            $this->setWarning("MSG303");
            return false;
        }

        $couse="合格者受験番号";
        $filename = sprintf("%s.csv",$couse);
        common::downloadContents($contents, $filename);
        return true;
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>
