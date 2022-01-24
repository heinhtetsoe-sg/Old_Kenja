<?php
/**
 * 定数定義
*/

class knjd126sModel extends Model {
    var $cmd;
    var $control;   //コントロールマスタ
    var $cookie_name;

    /**
     * メンバ変数の初期化
     */
    function knjd126sModel() {
        
    }

    function init() {
        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            $this->data = array();
            $this->testcdArray = array();
            common::GetControlMaster_Fnc($this->control);
        }
        $this->field=array("TABLE_NAME"       =>  VARS::post("TABLE_NAME") );
        $this->cmd = VARS::request("cmd");
    }

    function getDownloadModel() {
        var_dump($this->field);

        /* テスト */ 
        $testTableName = "v_name_mst"; $saveCorrFile = "Corr.csv"; $saveFileName = "tableName.csv";

        //csvファイル(PRGID, プログラム格納ディレクトリへのパス)作成
        $csvResult = $this->saveTargetFilePath($saveFileName, "/Query.inc$/");

        if (!$csvResult) {
            echo "csv書き込み失敗";
            exit;
        }

        /*csvファイルから一行ずつ読み込み*/
        $fp = fopen($saveFileName, "r");

        if (!$fp) {
            echo "ファイル読み込み失敗";
            exit;
        }

        $testArr = array();

        while (($resultArr = fgetcsv($fp)) !== false) {

            list($prgid, $sqlFilePath) = $resultArr;
            $searchResult = $this->searchTableNameInFile($sqlFilePath,  $testTableName);
            
            if (!$searchResult) {
                continue;
            }
            //ファイル内にテーブル名があった場合
           $testArr[] = $prgid;
           echo $prgid;
        }

        fclose($fp);
die();
        $contents = "";
        foreach ($testArr as $value) {
            $contents .= $testTableName.",".$value;
        }

        //ＣＳＶ出力
        $save_file_name = "テーブル名検索".date(YmdHis);
        $filename = sprintf("%s.csv",$save_file_name);
        common::downloadContents($contents, $filename);

/*
        
        echo "<pre>";

        var_dump($testArr);

        echo "</pre>";
*/
    }


    /* ディレクトリ探索用関数 */
    function saveTargetFilePath($saveFileName, $targetFilePattern) {
        
        $pathStack = array();
        $pathStack[] = DOCUMENTROOT;
        
        $documentrootFiles = $this->getFilesUnderDir($pathStack[0]);
        
        //DOCUMENTROOT直下のA～Zのディレクトリを取得
        $alphabetDirs = preg_grep("/^[A-Z]$/", $documentrootFiles);
        sort($alphabetDirs);
        
        $fp = fopen($saveFileName, "w");
        if ($fp) {
            foreach ($alphabetDirs as $alphabet) {
                
                //A～Z直下のKNJ●●ディレクトリ一覧を取得
                $pathStack[] = $alphabet;
                $path = implode("/", $pathStack);
                $pgdirs = $this->getFilesUnderDir($path);
                
                foreach ($pgdirs as $pgdir) {
                    //echo "(".$pgdir.")<br>";
                    
                    //KNJ●●直下のファイル一覧取得
                    $pathStack[] = $pgdir;
                    $path = implode("/", $pathStack);
                    $files = $this->getFilesUnderDir($path);
                    
                    foreach ($files as $file) {
                        //ファイル名がパターンと一致した場合
                        if (preg_match($targetFilePattern, $file)) {
                            $filePath = $path."/".$file.PHP_EOL;
                            //echo $filePath."<br>";
                            fwrite($fp, $pgdir.",".$filePath);
                        }
                        
                    }
                    
                    array_pop($pathStack);
                    
                }
                
                array_pop($pathStack);
                
            }
            
            fclose($fp);
            //echo "csv書き込みが終了しました";
            return true;
        }
        return false;
    }
    
    function getFilesUnderDir($root) {
        $fileArr = array();
        if (!is_dir($root)) return $fileArr;
        
        $h = opendir($root);
        while (($file = readdir($h)) !== false) {
            if ($file === "." || $file === "..")    continue;
            
            $fileArr[] = $file;
        }
        closedir($h);
        
        return $fileArr;
    }


    /* ファイル内探索用関数 */
    function searchTableNameInFile($filename, $targetTableName) {
        
        $multiLineCommentFlg = false;
        $targetTableName = mb_strtolower($targetTableName);
        
        $fp = @fopen($filename, "r");
        if ($fp) {
            while ($line = fgets($fp)) {
                
                mb_language("Japanese");
                $line = mb_convert_encoding($line, "UTF-8", "auto");
                $line = preg_replace('/\r\n/', "\n", $line);
                $line = preg_replace('/\r/',   "\n", $line);
                
                $symbols = array("/*", "//", "#");
                list($leftmostIndex, $leftmostPos) = $this->getLeftmostSymbol($line, $symbols);
                
                //対象行が複数コメントの終了行の場合
                //※EndOf～からチェックするのはcommentFlgがonの時は対象行は見ずに飛ばすので
                //複数行が終わったかどうかを先にチェックしないと(*/)の右まで無視してしまうため
                if ($multiLineCommentFlg && $this->isEndOfMultiLine($line)) {
                    $multiLineCommentFlg = false;
                    $pos = mb_strpos($line, "*/");
                    $line = $this->getRightStr($line, $pos, "*/");
                }
                
                //複数行コメント内の行なので見ずに飛ばす
                if ($multiLineCommentFlg) {
                    continue;
                }
                
                //対象行が複数行コメントの開始行の場合
                if (!$multiLineCommentFlg && $this->isStartOfMultiLine($line, $leftmostIndex, $symbols)) {
                    $multiLineCommentFlg = true;
                    $line = $this->getLeftStr($line, $leftmostPos);
                }
                
                /*
                 *　対象行の中に探しているテーブル名が含まれているかチェック
                 *ふくまれているようならそのテーブル名がコメントアウトされているかの判定を行う
                 */
                
                $line = preg_replace("/\/\*.*?\*\//", "", $line);
                
                //テーブル名が大文字小文字でも対応できるようにあらかじめ全て小文字にしておく
                $line = mb_strtolower($line);
                
                list($leftmostIndex, $leftmostPos) = $this->getLeftmostSymbol($line, $symbols);
                $tableNamePos = mb_strpos($line, $targetTableName);
                
                if ($tableNamePos === false) {                    
                    //対象行には目的のテーブル名は含まれていないので飛ばす
                    continue;
                }
                
                /*
                 *
                 * ここから↓テーブル名を取得できるところ
                 */
                
                if (is_null($leftmostIndex) || $leftmostPos > $tableNamePos) {
                    //対象行に記号が存在しない　（テーブル名のみ）
                    //もしくは
                    //テーブル名が記号より左　（例：　tablename //コメント　#コメント /*コメント*/）
                    
                    //目的のテーブルを含んでいるのでtrueを返して「searchTableNameInFile」を終了する
                    //（戻り値）
                    return true;
                }
                
                /*
                 * ここより↓はテーブル名がコメントアウトされている場合の箇所
                 * ただし、 /* * /　の右側にある場合にはテーブル名を返す
                 */
                
                //記号が (/*) の場合はleftmostPosより右であっても closeSymbolPosより右ならコメントではない
                if ($symbols[$leftmostIndex] === "/*") {
                    $commentCloseSymbolPos = mb_strpos($line, "*/");
                    if ($commentCloseSymbolPos !== false) {
                        if ($commentCloseSymbolPos < $tableNamePos) {
                            return true;
                        }
                    }
                }
                
            }
            fclose($fp);
        }
        return false;
    }
    
    function isStartOfMultiLine($line, $leftmostIndex, $symbols) {
        
        /*
         * 複数行コメントの中に含まれず　かつ　一番左に (/*)　が来たとき、
         * さらに同じ行に　終端(* /)がでてこないときに
         * この記号は複数行コメントの始点となる
         */
        
        if (!is_null($leftmostIndex) && $symbols[$leftmostIndex] === "/*") {
            $pos = mb_strpos($line, "*/");
            if ($pos === false) {
                return true;
            }
        }
        
        return false;
    }
    
    function isEndOfMultiLine($line) {
        
        /*
         * 複数行コメントの中に含まれて　かつ　(* /)　が来たとき、
         * この記号は複数行コメントの終端となる
         */
        
        $commentEndSymbolPos = mb_strpos($line, "*/");
        if ($commentEndSymbolPos !== false) {
            return true;
        }
        
        return false;
    }
    
    function getLeftStr($line, $pos) {
        $str = mb_substr($line, 0, $pos);
        return $str;
    }
    
    function getRightStr($line, $pos, $delimiter) {
        $str = mb_substr($line, $pos + strlen($delimiter));
        return $str;
    }
    
    //最も左端のコメント記号の「種類」と「文字位置」を取得
    function getLeftmostSymbol($targetLine, $symbols) {
        if (!isset($symbols) || !is_array($symbols)){
            echo "第二引数に配列を代入してください";
            return null;
        }
        
        $leftmostPos = 999;
        $leftmostIndex = null;
        
        foreach ($symbols as $index => $value) {
            
            $pos =  mb_strpos($targetLine, $value);
            
            if ($pos === false) continue;
            
            if ($pos < $leftmostPos) {
                $leftmostPos = $pos;
                $leftmostIndex = $index;
            }
        }
        
        return array($leftmostIndex, $leftmostPos);
    }

}
?>
