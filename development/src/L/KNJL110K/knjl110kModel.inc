<?php

require_once('for_php7.php');

class knjl110kModel extends Model {
    var $examcourse;
    var $cmd;
    var $examyear;

    function knjl110kModel()
    {
        $this->examyear = CTRL_YEAR + 1;
    }

    function init()
    {
        $this->cmd      = VARS::request("cmd");
        $this->testdiv  = VARS::post("TESTDIV");
    }

    function getDownloadModel()
    {
        /* 2005/09/01 arakaki
        $header = array("L_EXAMNO"    => "受験番号",
                        "L_SUCCOURSE" => "合格コース",
                        "BLANK"       => " ",
                        "R_EXAMNO"    => "受験番号",
                        "R_SUCCOURSE" => "合格コース",
                        );
        $contents = implode($header, ",")."\n";
        */

#        $data = false;

        $db = Query::dbCheckOut();

        //中学・高校判定---2005.09.01 minei
        $jhflg = $db->getOne("SELECT COUNT(*) FROM school_mst WHERE year = '".CTRL_YEAR."' AND schoolname1 LIKE '%中学%'");

        //SQL文発行
        $query = knjl110kQuery::selectCvsQuery($this,$jhflg);
        $result = $db->query($query);

        //header行 2005/09/01 arakaki
        //高校用
        if($jhflg == 0){
            $header = array("L_EXAMNO"    => "受験番号",
                            "L_SUCCOURSE" => "合格コース",
                            );
        //中学用
        }else{
            $header = array("L_EXAMNO"    => "受験番号",
                            "L_SUCCOURSE" => "合格コース",
                            "BLANK"       => " ",
                            "R_EXAMNO"    => "受験番号",
                            "R_SUCCOURSE" => "合格コース",
                            );
        }
        $contents = implode($header, ",")."\n";

        $null_line = 10;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            // 2005.09.01 minei
            //高校用
            if($jhflg == 0){
                if($row["PASS"] == 1){      //合否判定(judgement)が１～６で表示
                    $contents .= $row["EXAMNO"].","
                                .$row["EXAMCOURSE_ABBV"]." \n";
                }else{                      //合否判定(judgement)が１～６以外は表示なし
                    $contents .= ", \n";
                }

            //中学用
            }else{
                $contents .= $row["L_EXAMNO"].","
                            .$row["L_EXAMCOURSE_ABBV"].","
                            .","
                            .$row["R_EXAMNO"].","
                            .$row["R_EXAMCOURSE_ABBV"]." \n";
                #空行2行作成
                if($row["L_RECORD_CNT02"] == $null_line){
                    $contents .= "\n\n";
                    $null_line=$null_line+13;
                }

            }

/* 2005.09.01  ↓commented out by minei 
            $contents .= $row["L_EXAMNO"].","
                        .$row["L_EXAMCOURSE_ABBV"].","
                        .","
                        .$row["R_EXAMNO"].","
                        .$row["R_EXAMCOURSE_ABBV"]." \n";

            #空行2行作成
            if($row["L_RECORD_CNT02"] == $null_line){
                $contents .= "\n\n";
                $null_line=$null_line+13;
            }

#2005/08/17
#            if($row["PASS"] == 1){      //合否判定(judgement)が１～６で表示
#                $contents .= $row["EXAMNO"].","
#                            .$row["EXAMCOURSE_ABBV"]." \n";
#            }else{                      //合否判定(judgement)が１～６以外は表示なし
#                $contents .= ", \n";
#            }
#2005/08/17
*/
            $data = true;
        }
        $result->free();

        //試験区分名称
        $testdivname = $db->getOne(knjl110kQuery::getTestdivName($this));
        Query::dbCheckIn($db);

        if($data){
            $filename = $this->examyear."_合格者（".$testdivname."）速報用.csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);

            return true;
        }else{
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }
}
?>
