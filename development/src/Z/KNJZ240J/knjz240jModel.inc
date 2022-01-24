<?php

require_once('for_php7.php');

class knjz240jModel extends Model
{
    //定数定義
    public $field = array();
    public $cmd;
    public $documentMstDocumentCdFlg;
    public $isPattern;
    public $schoolName;

    //メンバ変数の初期化
    public function knjz240jModel()
    {
        $this->cmd = "";
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->field);
        }

        $this->field = array("DOCUMENTCD"   => VARS::post("DOCUMENTCD"),  //文書種類コンボ
                             "TITLE"        => VARS::post("TITLE"),       //件名
                             "CERTIF_NO"    => VARS::post("CERTIF_NO"),   //発行番号
                             "TEXT"         => VARS::post("TEXT"),        //本文
                             "DOCUMENT_DIV" => VARS::post("DOCUMENT_DIV"),//保険・その他切替ラジオボタン
                             "PATTERN"      => VARS::post("PATTERN")      //パターン数（テキストエリアの数）
        );

        $this->cmd = VARS::post("cmd");

        if (empty($this->field['PATTERN'])) {
            $this->field['PATTERN'] = 1;
        }

        if (isset($this->field['PATTERN']) && $this->field['PATTERN']>1) {
            for ($i = 0; $i<$this->field['PATTERN'] - 1; $i++) {
                $temp = VARS::post("TEXT_" . $i);
                if (isset($temp)) {
                    $this->field['LIST']["TEXT_" . $i] = VARS::post("TEXT_" . $i);
                }
            }
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //初期値の設定
        $this->documentMstDocumentCdFlg = (strlen($this->Properties["documentMstDocumentCdFlg"])) ? $this->Properties["documentMstDocumentCdFlg"] : 0;
        $this->documentMstDocumentDiv = (strlen($this->Properties["documentMstDocumentDiv"])) ? $this->Properties["documentMstDocumentDiv"] : 0;

        //文字数セット
        $this->setMojiSuu();

        $this->isPattern = ($this->documentMstDocumentCdFlg == 0) && knjz240jQuery::isKumamoto();

        $db = Query::dbCheckOut();
        $this->schoolName = $db->getOne(knjz240jquery::getNameMstZ010());
        Query::dbCheckIn($db);
    }

    public function setMojiSuu()
    {
        //本文文字数セット
        if ($this->field["DOCUMENT_DIV"] == '1') { //保健
            $doCd = $this->field["DOCUMENTCD"] * 1;
            if ($this->schoolName == 'sakae') {
                if ($this->Properties["useTextSizeKNJF030G_{$doCd}"]) {
                    list($moji, $gyou) = preg_split("/\*/", $this->Properties["useTextSizeKNJF030G_{$doCd}"]);
                    $this->text_moji = (int)trim($moji);
                    $this->text_gyou = (int)trim($gyou);
                } else { //初期値セット
                    switch ($this->field["DOCUMENTCD"]) {
                        case "04":
                            $this->text_moji = 40;
                            $this->text_gyou = 5;
                            break;
                        case "05":
                            $this->text_moji = 40;
                            $this->text_gyou = 5;
                            break;
                        case "06":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "07":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "08":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "09":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "10":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "11":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "12":
                            $this->text_moji = 40;
                            $this->text_gyou = 5;
                            break;
                        case "13":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "14":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "15":
                            $this->text_moji = 40;
                            $this->text_gyou = 5;
                            break;
                        case "16":
                            $this->text_moji = 40;
                            $this->text_gyou = 5;
                            break;
                        case "17":
                            $this->text_moji = 40;
                            $this->text_gyou = 5;
                            break;
                        case "18":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "19":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "20":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "21":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "22":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "23":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "24":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "25":
                            $this->text_moji = 40;
                            $this->text_gyou = 5;
                            break;
                        case "26":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "27":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "28":
                            $this->text_moji = 40;
                            $this->text_gyou = 5;
                            break;
                        case "29":
                            $this->text_moji = 40;
                            $this->text_gyou = 5;
                            break;
                        default:
                            break;
                    }
                }
            } elseif ($this->documentMstDocumentCdFlg == '2') {
                if ($this->Properties["useTextSizeKNJF030D_{$doCd}"]) {
                    list($moji, $gyou) = preg_split("/\*/", $this->Properties["useTextSizeKNJF030D_{$doCd}"]);
                    $this->text_moji = (int)trim($moji);
                    $this->text_gyou = (int)trim($gyou);
                } else { //初期値セット
                    switch ($this->field["DOCUMENTCD"]) {
                        case "03":
                            $this->text_moji = 40;
                            $this->text_gyou = 4;
                            break;
                        case "04":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "05":
                            $this->text_moji = 40;
                            $this->text_gyou = 4;
                            break;
                        case "06":
                            $this->text_moji = 40;
                            $this->text_gyou = 5;
                            break;
                        case "07":
                            $this->text_moji = 40;
                            $this->text_gyou = 4;
                            break;
                        case "08":
                            $this->text_moji = 40;
                            $this->text_gyou = 3;
                            break;
                        case "09":
                            $this->text_moji = 40;
                            $this->text_gyou = 4;
                            break;
                        case "10":
                            $this->text_moji = 40;
                            $this->text_gyou = 4;
                            break;
                        case "11":
                            $this->text_moji = 40;
                            $this->text_gyou = 5;
                            break;
                        case "12":
                            $this->text_moji = 40;
                            $this->text_gyou = 4;
                            break;
                        case "13":
                            $this->text_moji = 40;
                            $this->text_gyou = 6;
                            break;
                        case "14":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "15":
                            $this->text_moji = 40;
                            $this->text_gyou = 6;
                            break;
                        case "16":
                            $this->text_moji = 40;
                            $this->text_gyou = 5;
                            break;
                        case "17":
                            $this->text_moji = 40;
                            $this->text_gyou = 7;
                            break;
                        case "18":
                            $this->text_moji = 40;
                            $this->text_gyou = 3;
                            break;
                        case "19":
                            $this->text_moji = 40;
                            $this->text_gyou = 3;
                            break;
                        default:
                            break;
                    }
                }
            } else {
                switch ($this->field["DOCUMENTCD"]) {
                    case "01":
                        if ($this->Properties["useTextSizeKNJF030_7"]) {  //1:定期健康診断のお知らせ
                            list($moji, $gyou) = preg_split("/\*/", $this->Properties["useTextSizeKNJF030_7"]);
                            $this->text_moji = (int)trim($moji);
                            $this->text_gyou = (int)trim($gyou);
                        } else {
                            $this->text_moji = 30;
                            $this->text_gyou = 6;
                        }
                        break;
                    case "02":
                        if ($this->Properties["useTextSizeKNJF030_4"]) {  //2:眼科受診のお知らせ
                            list($moji, $gyou) = preg_split("/\*/", $this->Properties["useTextSizeKNJF030_4"]);
                            $this->text_moji = (int)trim($moji);
                            $this->text_gyou = (int)trim($gyou);
                        } else {
                            $this->text_moji = 30;
                            $this->text_gyou = 6;
                        }
                        break;
                    case "03":
                        if ($this->Properties["useTextSizeKNJF030_3"]) {  //3:健康診断の未受検項目のある生徒へ
                            list($moji, $gyou) = preg_split("/\*/", $this->Properties["useTextSizeKNJF030_3"]);
                            $this->text_moji = (int)trim($moji);
                            $this->text_gyou = (int)trim($gyou);
                        } else {
                            $this->text_moji = 30;
                            $this->text_gyou = 6;
                        }
                        break;
                    case "04":
                        if ($this->Properties["useTextSizeKNJF030_5"]) {  //4:検診結果のお知らせ
                            list($moji, $gyou) = preg_split("/\*/", $this->Properties["useTextSizeKNJF030_5"]);
                            $this->text_moji = (int)trim($moji);
                            $this->text_gyou = (int)trim($gyou);
                        } else {
                            $this->text_moji = 30;
                            $this->text_gyou = 6;
                        }
                        break;
                    default:
                        break;
                }
                if ($this->documentMstDocumentCdFlg == '1') {
                    switch ($this->field["DOCUMENTCD"]) {
                        case "05":
                            if ($this->Properties["useTextSizeKNJF030C_4"]) {  //5:眼科検診のお知らせ
                                list($moji, $gyou) = preg_split("/\*/", $this->Properties["useTextSizeKNJF030C_4"]);
                                $this->text_moji = (int)trim($moji);
                                $this->text_gyou = (int)trim($gyou);
                            } else {
                                $this->text_moji = 40;
                                $this->text_gyou = 5;
                            }
                            break;
                        case "06":
                            if ($this->Properties["useTextSizeKNJF030C_5"]) {  //6:検診結果のお知らせ(歯・口腔)1
                                list($moji, $gyou) = preg_split("/\*/", $this->Properties["useTextSizeKNJF030C_5"]);
                                $this->text_moji = (int)trim($moji);
                                $this->text_gyou = (int)trim($gyou);
                            } else {
                                $this->text_moji = 40;
                                $this->text_gyou = 6;
                            }
                            break;
                        case "07":
                            if ($this->Properties["useTextSizeKNJF030C_5A"]) {  //7:検診結果のお知らせ(歯・口腔)2
                                list($moji, $gyou) = preg_split("/\*/", $this->Properties["useTextSizeKNJF030C_5A"]);
                                $this->text_moji = (int)trim($moji);
                                $this->text_gyou = (int)trim($gyou);
                            } else {
                                $this->text_moji = 40;
                                $this->text_gyou = 7;
                            }
                            break;
                        case "08":
                            if ($this->Properties["useTextSizeKNJF030C_6"]) {  //8:定期健康診断の結果について
                                list($moji, $gyou) = preg_split("/\*/", $this->Properties["useTextSizeKNJF030C_6"]);
                                $this->text_moji = (int)trim($moji);
                                $this->text_gyou = (int)trim($gyou);
                            } else {
                                $this->text_moji = 40;
                                $this->text_gyou = 8;
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        } elseif ($this->field["DOCUMENT_DIV"] == '4') { //入試
            $this->text_moji = 38;
            $this->text_gyou = 12;
        } else { //学校徴収金
            if ($this->Properties["documentMstSize_{$this->field["DOCUMENTCD"]}"]) {
                list($moji, $gyou) = preg_split("/\*/", $this->Properties["documentMstSize_{$this->field["DOCUMENTCD"]}"]);
                $this->text_moji = (int)trim($moji);
                $this->text_gyou = (int)trim($gyou);
            } else {
                $this->text_moji = 40;
                $this->text_gyou = 6;
            }
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if (strlen($this->field["TITLE"]) > 120) {
            $this->setWarning("MSG914", "(件名は120バイトまでです)");
            return false;
        }

        if (strlen($this->field["CERTIF_NO"]) > 120) {
            $this->setWarning("MSG914", "(発行番号は120バイトまでです)");
            return false;
        }

        if ($this->validate_row_cnt($this->field["TEXT"], $this->text_moji * 2) > $this->text_gyou) {
            $this->setWarning("MSG914", "本文は全角{$this->text_moji}文字{$this->text_gyou}行までです。");
            return false;
        }

        if (isset($this->field['PATTERN']) && $this->field['PATTERN'] > 1) {
            for ($i = 0; $i<$this->field['PATTERN'] - 1; $i++) {
                if (strlen($this->field['LIST']["TEXT_" . $i]) > 5018) {
                    $this->setWarning("MSG914", "(本文は5018バイトまでです)");
                    return false;
                }
            }
        }
        return true;
    }

    //編集完了用のモデルを返します
    public function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->delete();
            $this->insert();
            $this->replace();
        }
    }

    //アップデート時の内部メソッド
    public function &insert()
    {
        $result = knjz240jQuery::getInsertQuery($this, $this->field);
        if (!isset($this->warning)) {
            $this->setMessage("MSG201");
        }
        return $result;
    }

    public function &delete()
    {
        $result = knjz240jQuery::getDeleteQuery($this->field);
        return $result;
    }

    public function &replace()
    {
        if ($this->isPattern) {
            $result = knjz240jQuery::getReplaceQuery($this->field);
        }
        return $result;
    }
}
