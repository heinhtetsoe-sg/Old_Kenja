<?php

require_once('for_php7.php');


class knja430s_2Model extends Model {
    var $cmd;
    var $inkan_no;
    var $req_inkan_no;
    var $scan_result;
    var $inkan_bmp;

    function knja430s_2Model()
    {
    }

    function init()
    {              
        $this->req_inkan_no = trim(VARS::get("inkan_no"));
        if (strlen($this->req_inkan_no)>0)
            $this->inkan_no = $this->req_inkan_no;
        else
            $this->inkan_no = VARS::post("inkan_no");
        $this->scan_result = VARS::post("scan_result");
        $this->inkan_bmp = VARS::post("inkan_bmp");
        $this->cmd = VARS::request("cmd");
//echo "scan_result[".$this->scan_result."]";
//echo " inkan_no[".$this->inkan_no."]";
    }

    /**
     * 送信モデルを返します
    */
    function &getExecModel()
    {
        if (strcasecmp($this->scan_result,"non") == 0){
           $this->warning = "走査画像が有りません!!";
        }
        else {
            $len = strlen($this->inkan_bmp);
            if ($len>0) {
                //echo "データ長 ".$len;
                $filename = DOCUMENTROOT ."/image/stamp/" .$this->inkan_no. ".bmp";
                //上書き不可は将来用//if (file_exists($filename)) {
                //    $this->warning =  "印影は既に読込済みなので取込めません!!";
                //}
                //else {
                    $data=base64_decode($this->inkan_bmp);
                    $volm = strlen(bin2hex($data)) / 2;
                    //echo "base64データ長".$volm;
                    $fp =fopen($filename,"w");
                    fwrite($fp, $data, $volm);
                    fclose($fp);
                    knja430s_2Query::getUpdateQueryInkan($this->inkan_no);
                    //chmod($filename, 0444);  //上書き不可
                    $this->scan_result = "***";
                    //$this->message = "印影の取込は正常です。";
                //}
            }
            else {
                $this->warning = "走査画像データが有りません!!";
            }
        }
    }

    function validateInput()
    {
        return true;
    }
}
?>
