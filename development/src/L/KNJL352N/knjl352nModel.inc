<?php

require_once('for_php7.php');

class knjl352nModel extends Model
{
    public $field      = array();
    public $cmd;
    public $ObjYear;
    public $oldYear1;
    public $oldYear2;
    public $lastColumn;//CSVの最後の列の値

    public function knjl352nModel()
    {
    }

    public function init()
    {
        $this->ObjYear = CTRL_YEAR+1;
        $this->oldYear1 = $this->ObjYear-1;
        $this->oldYear2 = $this->ObjYear-2;

        $this->field = array("APPLICANTDIV"       =>  VARS::post("APPLICANTDIV")    //入試制度
                     );

        $this->cmd         = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //和暦年度
        $getNendo  = common::DateConv1($this->ObjYear ."/04/01", "2")."度";
        $oldNendo1 = common::DateConv1($this->oldYear1."/04/01", "2")."度";
        $oldNendo2 = common::DateConv1($this->oldYear2."/04/01", "2")."度";
        //日付
        $getDateWareki = common::DateConv1(str_replace("-", "/", CTRL_DATE), "0");
        //時間
        $getTimeWareki = date("G時i分", time());

        //ヘッダデータ
        $header =  array("1"    => $oldNendo2."推薦",
                         "2"    => $oldNendo2."専願",
                         "3"    => $oldNendo2."一般",
                         "4"    => $oldNendo2."受験者",
                         "5"    => $oldNendo1."推薦",
                         "6"    => $oldNendo1."専願",
                         "7"    => $oldNendo1."一般",
                         "8"    => $oldNendo1."受験者",
                         "9"    => $getNendo."推薦",
                         "10"   => $getNendo."専願",
                         "11"   => $getNendo."一般",
                         "12"   => $getNendo."受験者",
                         "13"   => "３ヶ年受験者合計"."推薦",
                         "14"   => "３ヶ年受験者合計"."専願",
                         "15"   => "３ヶ年受験者合計"."一般",
                         "16"   => "３ヶ年受験者合計"."受験者",
                         "17"   => "中学校",
                         "18"   => "３ヶ年入学者合計",
                         "19"   => $oldNendo2."入学",
                         "20"   => $oldNendo1."入学",
                         "21"   => $getNendo."入学",
                         "22"   => "今年度の内訳"."推薦",
                         "23"   => "今年度の内訳"."専願",
                         "24"   => "今年度の内訳"."一般",
                         "25"   => $this->lastColumn);

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        $contents .= $getNendo."　高等学校入試　生徒募集部資料　　　".$getDateWareki."　".$getTimeWareki."現在"."\n";
        $contents .= implode(",", $header) ."\n";

        //データ
        $query = knjl352nQuery::selectMainQuery($this);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);

            $contents .= implode(",", $row) ."\n";
            $header_data = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //ＣＳＶ出力
        if ($header_data) {
            $couse = "生徒募集部資料";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}
