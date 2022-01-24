<?php

require_once('for_php7.php');
//定数定義
function padding( $str, $pad_length)
{
    $str = mb_convert_encoding($str, "SJIS-win", "UTF-8");
    $str = sprintf("%-".$pad_length ."s", $str);    //左詰め
    $str = mb_convert_encoding($str, "UTF-8", "SJIS-win");
    return $str;
} 

class knjp080kModel extends Model {
    var $cmd;
    var $selectdata;

    //メンバ変数の初期化
    function knjp080kModel()
    {
    }
    function init()
    {
        $this->selectdata        = VARS::post("selectdata");
        if ($this->selectdata) {
            $this->hr_class = explode(",", $this->selectdata);
        }
        $this->cmd              = VARS::request("cmd");
        $this->outformat        = VARS::post("OUTFORMAT");
        $this->radio            = VARS::post("RADIO");
    }

    //入力項目チェック
    function validateInput()
    {
        return true;
    }
    //西暦ー＞和暦変換
    function chgDate($date){
        list($y,$m,$d) = explode("-", $date);
        $g = common::Calc_Wareki($y,$m,$d);
        $g = substr($g,0,6);
        return sprintf("%d%02d%02d%02d",$this->gengou[$g], $y,$m,$d);
    }
    //編集完了用のモデルを返します
    function &getExecuteModel()
    {
        $db = Query::dbCheckOut();

        $schooldiv = 0;
        $query = knjp080kQuery::getschool();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schooldiv = 1;
        }
        $result->free();

        $schoolcd = $db->getOne(knjp080kQuery::selectQueryIntData1($this));
        //元号取得
        $result = $db->query(knjp080kQuery::selectQueryGengou($this));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->gengou[$row["NAME1"]] = $row["NAMECD2"];
        }
        $result = $db->query(knjp080kQuery::selectExecQuery1($this,$schoolcd));
        $data = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //一年用（独自フォーマット）
            if ($this->outformat == 1) {
                $data .= padding($row["BANK_MAJORCD"], 2).padding($row["GRADE"], 2).padding($row["BANK_HR_CLASS"], 2);   //CRSCODE(6)
                $data .= padding(substr($row["SCHREGNO"],0,2), 2);                  //NENDO(2)
                $data .= padding(substr($row["SCHREGNO"],2,2), 2);                  //NAME_CODE(2)

                if (CTRL_YEAR == "2006" && $schooldiv == "1" && $row["GRADE"] == "01" && $row["REMAINGRADE_FLG"] != "01") {
                    $data .= sprintf("%04d",substr($row["SCHREGNO"],4,4) - 5000, 4);//KEYNO(4)
                } else {
                    $data .= sprintf("%04d",substr($row["SCHREGNO"],4,4), 4) ;      //KEYNO(4)
                }

                $data .= ' ';                                                       //SIBOUKA(1)
                $data .= ' ';                                                       //SENPEI(1)
                $data .= ' ';                                                       //KUBUN(1)
                $data .= padding(mb_convert_kana($row["NAME_KANA"], "hs"), 20);     //NAMEKNA(20)
                $data .= padding($row["NAME"], 20);                                 //NAMEKNJ(20)
                $data .= padding($row["SEX"], 1);                                   //SEIBETU(1)
                $data .= padding($this->chgDate($row["BIRTHDAY"]), 7);              //NENGOU(1)+BIRTHDAY(6)
                $data .= "  ";                                                      //F_CODE_A(2)
                $data .= "  ";                                                      //F_CODE_C(2)
                $data .= " ";                                                       //SETURIKU(1)
                $data .= "  ";                                                      //SOTUNEN(2)
                $data .= "  ";                                                      //GAKKU(2)
                $data .= "     ";                                                   //CHUCODE(5)
                $data .= " ";                                                       //YOHAKU1(1)
                $data .= sprintf("%-7s",str_replace("-","",$row["ZIPCD"]));         //YUBIN_NO_1(7)
                $data .= str_repeat(" ",60);                                        //JYUSHO_KANA1(60)
                $data .= padding($row["ADDR1"].$row["ADDR2"], 100);                 //JYUSHO_KNJI1(100)
                $data .= padding($row["TELNO"], 12);                                //TEL1(12)
                $data .= padding(mb_convert_kana($row["GUARD_KANA"], "hs"), 16);    //PNAMEKNA(16)
                $data .= "    ";                                                    //YOHAKU1(4)
                $data .= padding($row["GUARD_NAME"], 20);                           //PNAMEKNJ(20)
                $data .= sprintf("%-7s",str_replace("-","",$row["GUARD_ZIPCD"]));   //YUBIN_NO_2(7)
                $data .= str_repeat(" ",60);                                        //JYUSHO_KANA2(60)
                $data .= padding($row["GUARD_ADDR1"].$row["GUARD_ADDR2"], 100);     //JYUSHO_KNJI2(100)
                $data .= padding($row["GUARD_TELNO"], 12);                          //TEL2(12)
                $data .= str_repeat(" ",6);                                         //FORMNO(6)
                $data .= str_repeat(" ",16);                                        //YOHAKU(16)
                $data .= "\n";                                                      //改行

            } else {
                $data .= '32';                              //HYO_NO(伝票番号)(2)
                $data .= padding($row["SCHOOLCD"], 5);      //INTDATA1(学校コード)(5)
                $data .= '  ';                              //GAKUBU(旧学部コード)(2)
                $data .= padding($row["BANK_MAJORCD2"], 2).padding($row["GRADE2"], 2).padding($row["BANK_HR_CLASS2"], 2);   //GAKKA(2),GAKUNEN(2),CLASS(2)
                $data .= padding($row["SCHREGNO_OLD"], 8);  //KJNCODE_0(学籍番号)(8)
                if ($row["GRADE"] == "01") {
                    $data .= "B" .padding($schoolcd, 5);    //INTDATA2(学校コード)(5)
                } else {
                    $data .= "A" .padding($schoolcd, 5);    //INTDATA2(学校コード)(5)
                }
                $data .= '  ';          //NGAKUBU(新学部コード)(2)
                $data .= padding($row["BANK_MAJORCD"].$row["GRADE"].$row["BANK_HR_CLASS"], 6);   //NGAKKA(2),NGAKUNEN(2),NCLASS(2)

                if (CTRL_YEAR == "2006" && $schooldiv == "1" && $row["GRADE"] == "01" && $row["REMAINGRADE_FLG"] != "01") {
                    $data .= padding($row["SCHREGNO"] - 5000, 8) ;  //KJNCODE_N(学籍番号)(8)
                } else {
                    $data .= padding($row["SCHREGNO"], 8) ;         //KJNCODE_N(学籍番号)(8)
                }

                $data .= '  '; //BLANK1(2)

                if ($schooldiv == "1") {
                    $row["SEX"] = "  ";
                } else {
                    $row["SEX"] = sprintf("%02d",str_replace("1","50",$row["SEX"]));
                    $row["SEX"] = sprintf("%02d",str_replace("2","51",$row["SEX"]));
                }
                $data .= $row["SEX"];           //SEIBETU(2)
                $data .= str_repeat(" ",337);   //BLANK3(337)
                $data .= '001';                 //頁 NO003
                $data .= str_repeat(" ",11);    //BLANK3(11)
                $data .= "\n";                  //改行
    
            }
        }
        Query::dbCheckIn($db);
        if ($data != "") {
            //一年用（独自フォーマット）
            if ($this->outformat == 1){
                $filename = "１年生用（独自フォーマット）.csv";
            } else {
                $filename = "２．３年生用（QW-32）.csv";
            }
            //ファイルダウンロード
            common::downloadContents($data, $filename);
            return true;
        } else {
            //データは存在していません。
            $this->setWarning("MSG303");

            return false;
        }
    }
}
?>
