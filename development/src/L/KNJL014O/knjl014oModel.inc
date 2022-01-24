<?php

require_once('for_php7.php');

class knjl014oModel extends Model
{
    public $field  = array();
    public $data   = array();
    public $cmd;
    public $file;
    public $ObjYear;

    public function init()
    {
        $this->ObjYear = CTRL_YEAR + 1;

        $this->field = array("APPLICANTDIV" =>  VARS::post("APPLICANTDIV"),
                             "TESTDIV"      =>  VARS::post("TESTDIV"),
                             "HEADER"       =>  VARS::post("HEADER")
                             );

        $this->cmd = VARS::request("cmd");

        if (VARS::file("FILE")) {
            $this->field["FILE"] = VARS::file("FILE");      //ファイルのアップロード
            $this->field["FILE"]["name"] = mb_convert_encoding($this->field["FILE"]["name"], "SJIS-win", "UTF-8");
        }
    }

    /********************/
    /*  ＣＳＶ出力処理  */
    /********************/
    public function getDownloadModel()
    {
        $header = array();

        $header[] = "※年度";
        $header[] = "出願申込番号";
        $header[] = "ログインユーザSEQ";
        $header[] = "メールアドレス";
        $header[] = "志願者SEQ";
        $header[] = "入試区分";
        $header[] = "※志願者氏名(漢字)（姓）";
        $header[] = "※志願者氏名(漢字)（名）";
        $header[] = "※志願者氏名(カナ)（姓）";
        $header[] = "※志願者氏名(カナ)（名）";
        $header[] = "※性別";
        $header[] = "生年月日";
        $header[] = "郵便番号";
        $header[] = "都道府県";
        $header[] = "市区町村";
        $header[] = "町名・番地";
        $header[] = "建物名・部屋番号";
        $header[] = "電話番号";
        $header[] = "学校名コード";
        $header[] = "学校名名称";
        $header[] = "学校名名称その他";
        $header[] = "第１志望校コード";
        $header[] = "第１志望校名称";
        $header[] = "第１志望校名称その他";
        $header[] = "第２志望校コード";
        $header[] = "第２志望校名称";
        $header[] = "第２志望校名称その他";
        $header[] = "第３志望校コード";
        $header[] = "第３志望校名称";
        $header[] = "第３志望校名称その他";
        $header[] = "第４志望校コード";
        $header[] = "第４志望校名称";
        $header[] = "第４志望校名称その他";
        $header[] = "塾コード";
        $header[] = "塾名称";
        $header[] = "塾名称その他";
        $header[] = "保護者氏名（漢字）（姓）";
        $header[] = "保護者氏名（漢字）（名）";
        $header[] = "保護者氏名（カナ）（姓）";
        $header[] = "保護者氏名（カナ）（名）";
        $header[] = "緊急連絡先（電話番号）";
        $header[] = "緊急連絡先（説明）例：父親携帯";
        $header[] = "出願情報状態区分";
        $header[] = "調査書受領区分";
        $header[] = "窓口受付番号";
        $header[] = "備考欄";
        $header[] = "決済金額";
        $header[] = "受験料";
        $header[] = "事務手数料";
        $header[] = "支払種別コード";
        $header[] = "支払期限日";
        $header[] = "入金日";
        $header[] = "入金情報区分";
        $header[] = "申込完了日";
        $header[] = "キャンセルフラグ";
        $header[] = "キャンセル日";
        $header[] = "本校を受験校に選定された理由は何ですか。（複数回答可）";
        $header[] = "「８.その他」を選択した方は詳細を入力してください。";
        $header[] = "本校の受験はいつ頃決めましたか。（例：小学５年生８月頃）";
        $header[] = "ご兄姉に本校在校生がいますか";
        $header[] = "本校在校生の氏名";
        $header[] = "本校在校生と志願者の関係";
        $header[] = "本校在校生の学年";
        $header[] = "ご家族に本校卒業生がいますか";
        $header[] = "本校卒業生の氏名※卒業時の氏名";
        $header[] = "本校卒業生と志願者の関係";
        $header[] = "卒業年度（西暦4桁）";
        $header[] = "自由記入欄※これまでの学習活動、課外活動、取得資格や志望理由、ＰＲ等";
        $header[] = "※試験番号";
        $header[] = "試験日";
        $header[] = "試験名";
        $header[] = "試験名略称";
        $header[] = "※受験番号_システム";
        $header[] = "受験番号_手動";

        //クリア
        $contents  = "";
        //ヘッダ出力
        $contents .= implode($header, ",")."\n";

        //ファイル名
        $title = $this->ObjYear.'年度 Webヘッダデータ';

        //ＣＳＶ出力
        $filename = sprintf("%s.csv", $title);
        common::downloadContents($contents, $filename);
        return true;
    }

    /********************/
    /*  ＣＳＶ取込処理  */
    /********************/
    public function &getExecModel()
    {
        $ercnt = 0;
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->GetFileString($ercnt);

        //データの整合性チェック
        if ($this->IsDataMatched($this->data_arr)) {
            $cnt = knjl014oQuery::updateQueryCsv($this, $this->data_arr);
            $this->setMessage("MSG202", "処理件数：{$cnt}件、エラー：{$ercnt}件");
        }
    }

    /*************************/
    /*  CSVファイルを配列へ  */
    /*************************/
    public function GetFileString(&$ercnt)
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->field["FILE"]["tmp_name"])) { //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            $fp = fopen($this->field["FILE"]["tmp_name"], "r");
            $i  = 0;
            $line = 1;
            $header = false;
            while (!feof($fp)) {
                $buffer = fgets($fp, 8192);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(mb_convert_encoding($buffer, mb_http_output(), "SJIS-win"));
                    $buffer    = str_replace("↓", "\n", $buffer);
                    $tmp    = str_replace('"', '', common::csv2array($buffer));
                    $cnt = 0;
                    $data = array();

                    $data["ENTEXAMYEAR"]         = $tmp[$cnt++];
                    $data["APPLICATION_NO"]      = $tmp[$cnt++];
                    $data["LOGIN_ID"]            = $tmp[$cnt++];
                    $data["EMAIL"]               = $tmp[$cnt++];
                    $data["VOLUNTEER"]           = $tmp[$cnt++];
                    $data["APPLICANTDIV"]        = $tmp[$cnt++];
                    $data["NAME_SEI"]            = $tmp[$cnt++];
                    $data["NAME_MEI"]            = $tmp[$cnt++];
                    $data["NAME_KANA_SEI"]       = $tmp[$cnt++];
                    $data["NAME_KANA_MEI"]       = $tmp[$cnt++];
                    $data["SEX"]                 = $tmp[$cnt++];
                    $data["BIRTHDAY"]            = $tmp[$cnt++];
                    $data["ZIPCD"]               = $tmp[$cnt++];
                    $data["ADDRESS1"]            = $tmp[$cnt++];
                    $data["ADDRESS2"]            = $tmp[$cnt++];
                    $data["ADDRESS3"]            = $tmp[$cnt++];
                    $data["ADDRESS4"]            = $tmp[$cnt++];
                    $data["TELNO"]               = $tmp[$cnt++];

                    $data["FS_CD"]               = $tmp[$cnt++];
                    $data["FS_NAME"]             = $tmp[$cnt++];
                    $data["FS_NAME_ETC"]         = $tmp[$cnt++];
                    $data["SHIBOU_CD1"]          = $tmp[$cnt++];
                    $data["SHIBOU_NAME1"]        = $tmp[$cnt++];
                    $data["SHIBOU_NAME_ETC1"]    = $tmp[$cnt++];

                    $data["SHIBOU_CD2"]          = $tmp[$cnt++];
                    $data["SHIBOU_NAME2"]        = $tmp[$cnt++];
                    $data["SHIBOU_NAME_ETC2"]    = $tmp[$cnt++];

                    $data["SHIBOU_CD3"]          = $tmp[$cnt++];
                    $data["SHIBOU_NAME3"]        = $tmp[$cnt++];
                    $data["SHIBOU_NAME_ETC3"]    = $tmp[$cnt++];

                    $data["SHIBOU_CD4"]          = $tmp[$cnt++];
                    $data["SHIBOU_NAME4"]        = $tmp[$cnt++];
                    $data["SHIBOU_NAME_ETC4"]    = $tmp[$cnt++];

                    $data["PRISCHOOLCD"]         = $tmp[$cnt++];
                    $data["PRISCHOOL_NAME"]      = $tmp[$cnt++];
                    $data["PRISCHOOL_NAME_ETC"]  = $tmp[$cnt++];

                    $data["GNAME_SEI"]           = $tmp[$cnt++];
                    $data["GNAME_MEI"]           = $tmp[$cnt++];
                    $data["GKANA_SEI"]           = $tmp[$cnt++];
                    $data["GKANA_MEI"]           = $tmp[$cnt++];
                    $data["GTELNO"]              = $tmp[$cnt++];
                    $data["GTELNO2"]             = $tmp[$cnt++];
                    $data["APPLICANT_INFO_DIV"]  = $tmp[$cnt++];

                    $data["TRANSCRIPT_DIV"]      = $tmp[$cnt++];
                    $data["WINDOW_RECEPTNO"]     = $tmp[$cnt++];
                    $data["REMARK"]              = $tmp[$cnt++];
                    $data["SETTLEMENT_FIGURE"]   = $tmp[$cnt++];
                    $data["EXAM_FEE"]            = $tmp[$cnt++];
                    $data["ADMINISTRATIVE_FEE"]  = $tmp[$cnt++];
                    $data["PAY_DIVCD"]           = $tmp[$cnt++];
                    $data["PAY_LIMITDATE"]       = $tmp[$cnt++];
                    $data["PAID_MONEY_DATE"]     = $tmp[$cnt++];
                    $data["PAID_INFO_DIV"]       = $tmp[$cnt++];
                    $data["REQUEST_COMP_DATE"]   = $tmp[$cnt++];
                    $data["CANCEL_FLG"]          = $tmp[$cnt++];
                    $data["CANCEL_DATE"]         = $tmp[$cnt++];

                    $data["RECEPT_REASON"]       = $tmp[$cnt++];
                    $data["ETC8"]                = $tmp[$cnt++];
                    $data["WHEN_DECISION"]       = $tmp[$cnt++];
                    $data["BRO_SCH_FLG"]         = $tmp[$cnt++];
                    $data["BRO_SCH_NAME"]        = $tmp[$cnt++];
                    $data["BRO_SCH_RELA"]        = $tmp[$cnt++];
                    $data["BRO_SCH_GRADE"]       = $tmp[$cnt++];
                    $data["BRO_SCH_GRD_FLG"]     = $tmp[$cnt++];
                    $data["BRO_SCH_GRD_NAME"]    = $tmp[$cnt++];
                    $data["BRO_SCH_GRD_RELA"]    = $tmp[$cnt++];

                    $data["BRO_SCH_GRD_YEAR"]    = $tmp[$cnt++];

                    $data["FREE_ENTRY"]          = $tmp[$cnt++];

                    $data["TESTDIV"]             = $tmp[$cnt++];
                    $data["TEST_DATE"]           = $tmp[$cnt++];
                    $data["TEST_NAME"]           = $tmp[$cnt++];
                    $data["TEST_NAME_ABBV"]      = $tmp[$cnt++];
                    $data["EXAMNO"]              = $tmp[$cnt++];
                    $data["EXAMNO_HAND"]         = $tmp[$cnt++];

                    //項目数によるファイルチェック
                    if ($this->ObjYear != $data["ENTEXAMYEAR"]) {
                        $this->warning = "CSVファイルを確認して下さい。\\n{$line}行目の入試年度が不一致です。";
                        return;
                    }
                    //必須項目チェック
                    $nnulchkflg = true;
                    if ($data["APPLICANTDIV"] == null || $data["TESTDIV"] == null || $data["EXAMNO"] == null) {
                        $nnulchkflg = false;
                    }

                    //項目サイズチェック
                    if ($nnulchkflg && knjl014oModel::datalengthcheck($data)) {
                        $temp_arr[$i] = $data;
                        $i++;
                        $line++;
                    } else {
                        $ercnt++;
                    }
                }
            }
            fclose($fp);
        }

        return $temp_arr;
    }

    //CSVデータのサイズチェック
    public function datalengthcheck($data)
    {
        //データ長
        $lentbl = array(
            "ENTEXAMYEAR"        => 4,
            "APPLICATION_NO"     => 20,
            "LOGIN_ID"           => 20,
            "EMAIL"              => 60,
            "VOLUNTEER"          => 20,
            "APPLICANTDIV"       => 30,
            "NAME_SEI"           => 120,
            "NAME_MEI"           => 120,
            "NAME_KANA_SEI"      => 120,
            "NAME_KANA_MEI"      => 120,
            "SEX"                => 3,
            "BIRTHDAY"           => 10,
            "ZIPCD"              => 8,
            "ADDRESS1"           => 30,
            "ADDRESS2"           => 150,
            "ADDRESS3"           => 150,
            "ADDRESS4"           => 150,
            "TELNO"              => 14,
            "FS_CD"              => 20,
            "FS_NAME"            => 120,
            "FS_NAME_ETC"        => 120,
            "SHIBOU_CD1"         => 20,
            "SHIBOU_NAME1"       => 120,
            "SHIBOU_NAME_ETC1"   => 120,
            "SHIBOU_CD2"         => 20,
            "SHIBOU_NAME2"       => 120,
            "SHIBOU_NAME_ETC2"   => 120,
            "SHIBOU_CD3"         => 20,
            "SHIBOU_NAME3"       => 120,
            "SHIBOU_NAME_ETC3"   => 120,
            "SHIBOU_CD4"         => 20,
            "SHIBOU_NAME4"       => 120,
            "SHIBOU_NAME_ETC4"   => 120,
            "PRISCHOOLCD"        => 20,
            "PRISCHOOL_NAME"     => 200,
            "PRISCHOOL_NAME_ETC" => 120,
            "GNAME_SEI"          => 120,
            "GNAME_MEI"          => 120,
            "GKANA_SEI"          => 120,
            "GKANA_MEI"          => 120,
            "GTELNO"             => 15,
            "GTELNO2"            => 120,
            "APPLICANT_INFO_DIV" => 15,
            "TRANSCRIPT_DIV"     => 15,
            "WINDOW_RECEPTNO"    => 20,
            "REMARK"             => 150,
            "SETTLEMENT_FIGURE"  => 7,
            "EXAM_FEE"           => 7,
            "ADMINISTRATIVE_FEE" => 7,
            "PAY_DIVCD"          => 65,
            "PAY_LIMITDATE"      => 20,
            "PAID_MONEY_DATE"    => 20,
            "PAID_INFO_DIV"      => 15,
            "REQUEST_COMP_DATE"  => 20,
            "CANCEL_FLG"         => 30,
            "CANCEL_DATE"        => 20,
            "RECEPT_REASON"      => 300,
            "ETC8"               => 700,
            "WHEN_DECISION"      => 90,
            "BRO_SCH_FLG"        => 90,
            "BRO_SCH_NAME"       => 120,
            "BRO_SCH_RELA"       => 10,
            "BRO_SCH_GRADE"      => 40,
            "BRO_SCH_GRD_FLG"    => 60,
            "BRO_SCH_GRD_NAME"   => 60,
            "BRO_SCH_GRD_RELA"   => 10,
            "BRO_SCH_GRD_YEAR"   => 4,
            "FREE_ENTRY"         => 1500,
            "TESTDIV"            => 2,
            "TEST_DATE"          => 20,
            "TEST_NAME"          => 40,
            "TEST_NAME_ABBV"     => 30,
            "EXAMNO"             => 10,
            "EXAMNO_HAND"        => 10
        );

        $chkflg = true;
        foreach ($lentbl as $key => $val) {
            if (strlen($data[$key]) > $lentbl[$key]) {
                $chkflg = false;
            }
        }

        return $chkflg;
    }

    /****************************/
    /*  データの整合性チェック  */
    /****************************/
    public function IsDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    public function &getUpdateModel()
    {
        $retmsg = knjl014oQuery::updateQuery($this);
        $this->setMessage("MSG201", $retmsg);
    }
}
