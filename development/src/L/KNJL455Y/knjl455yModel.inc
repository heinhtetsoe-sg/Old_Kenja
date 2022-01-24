<?php

require_once('for_php7.php');
/**
 * 定数定義
*/

class knjl455yModel extends Model
{
    public $field      = array();
    public $cmd;
    public $ObjYear;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knjl455yModel()
    {
    }

    public function init()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear      = CTRL_YEAR+1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),      //入試制度
                             "OUTPUT"       =>  VARS::post("OUTPUT")             //出力順ラジオ
                            );

        $this->cmd = VARS::request("cmd");

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

    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //タイトル
        list($loginYear, $loginMon, $loginDay) = explode("-", str_replace("/", "-", CTRL_DATE));
        $loginDate  = $loginYear."年".sprintf("%d", $loginMon)."月".sprintf("%d", $loginDay)."日";
        $tilteName  = $this->ObjYear."年度　入学試験　入学者名簿";
        $header1 = array("NUMBER"            => $tilteName,
                         "EXAMNO"            => "",
                         "NAME"              => "",
                         "SEX_NAME"          => "",
                         "FS_NAME"           => $loginDate,
                         "GNAME"             => "",
                         "ZIPCD"             => "",
                         "ADDRESS"           => "",
                         "TELNO"             => "",
                         "REMARK"            => "",
                         $this->lastColumn   => "");
        //ヘッダ
        $header = array("NUMBER"            => "No.",
                        "EXAMNO"            => "受験番号",
                        "NAME"              => "生徒氏名",
                        "SEX_NAME"          => "性別",
                        "FS_NAME"           => "出身校",
                        "GNAME"             => "保護者名",
                        "ZIPCD"             => "郵便番号",
                        "ADDRESS"           => "現住所",
                        "TELNO"             => "電話番号",
                        "REMARK"            => "備考",
                        $this->lastColumn   => $this->lastColumn);

        $contents  = "";
        $contents .= implode($header1, ",") ."\n";
        $contents .= implode($header, ",") ."\n";

        //CSVデータ取得
        $number  = 0;
        $dataFlg = false;
        $query  = knjl455yQuery::getCsvQuery($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace("\"", "", $row);//データ内にダブルクォーテーションがあればブランクに置換
            $row["NUMBER"]  = ++$number;
            $row["REMARK"]  = "";
            $row[$this->lastColumn]   = $this->lastColumn;

            foreach ($header as $key => $val) {
                $contents .= $row[$key] .",";
            }
            $contents .= "\n";

            $dataFlg = true;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($dataFlg) {
            //ファイル名
            $filename = $this->ObjYear."年度小学入学試験入学者名簿.csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");    //データは存在していません。
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
